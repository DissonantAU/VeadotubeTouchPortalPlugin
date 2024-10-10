package io.github.dissonantau.bleatkan.instance


import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import io.github.dissonantau.bleatkan.MiscFunctions.getUnixTime
import io.github.dissonantau.bleatkan.message.VtInstance
import java.io.FileReader
import java.io.IOException
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.time.Instant
import kotlin.io.path.name


/**
 * Checks Veadotube Instances Folder, Tracks Instances, and generates events for new/changed/closed Instances
 *
 *
 * Originally based on Instances.cs in [veadotube bleatcan (Commit b1d4f...)](https://gitlab.com/veadotube/bleatcan/-/tree/b1d4faf70138c1e839b449c3cf799b6fd59c837b/bleatcan)
 */
@Suppress("unused")
class InstancesManager(
    listener: InstancesListener,
    managerJobParent: Job? = null
) : AutoCloseable {

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        /** Timestamp Timeout in Seconds
         *
         * An Instance File with an internal timestamp older than 10s is considered out of date/dead */
        const val READ_TIMEOUT_SEC: Long = 10

        /** Max/Targeted time to sleep between loops (milliseconds)
         *
         * Actual Delay will be the ***largest*** of [READ_LOOP_DELAY_MAX_MS] minus `loop runtime`, and [READ_LOOP_DELAY_MIN_MS] */
        const val READ_LOOP_DELAY_MAX_MS: Long = 3 * 1000

        /** Minimum time to sleep between loops (milliseconds)*/
        const val READ_LOOP_DELAY_MIN_MS: Long = 100

        /* static vals to be initialised */
        /** Directory - Veadotube Instances (at <user profile/home>\.veadotube\instances\) */
        private val dirInstances: Path

        init {

            val dirHomeFolder: String = System.getProperty("user.home")
            LOGGER.trace { "Home Folder = '$dirHomeFolder'" }

            val dirVeadotubeInstances = Paths.get(dirHomeFolder, ".veadotube", "instances")
            LOGGER.info { "Veadotube Instances Folder = '$dirVeadotubeInstances'" }

            // If it doesn't exist, create
            if (!dirVeadotubeInstances.toFile().isDirectory) {
                Files.createDirectories(dirVeadotubeInstances)
            }

            dirInstances = dirVeadotubeInstances
        }

    }

    constructor(
        listener: InstancesListener
    ) : this(listener = listener, managerJobParent = null)

    /**
     * Coroutine Job - contains all Jobs for InstancesManager
     */
    private val instMgrJob: CompletableJob =
        if (managerJobParent != null)
            SupervisorJob(managerJobParent)
        else
            SupervisorJob()

    /**
     * Coroutine Dispatcher for Checker
     */
    private val instanceCheckerDispatcher: CoroutineDispatcher =
        Dispatchers.Default.limitedParallelism(1, "instancesManager-dsp")

    /**
     * Coroutine Dispatcher for Reader
     */
    private val instanceReaderDispatcher: CoroutineDispatcher =
        Dispatchers.IO.limitedParallelism(1, "instanceReaderDispatcher")

    /**
     * Scope for this Connection, should be used to launch Jobs
     */
    private val instMgrScope: CoroutineScope =
        CoroutineScope(instMgrJob + CoroutineName("instancesManager-cr"))


    /** Filename, Instance ID Object */
    private val instancesIDMap = HashMap<String, InstanceID>()

    /** Instance ID Object, Instance Object */
    private val instancesMap = HashMap<InstanceID, Instance>()

    /** Mutex for Instances Map */
    private val instancesMapMutex = Mutex()

    /** Event Listener that should receive Start/Change/End events */
    private val instanceEventListener: InstancesListener = listener


    /** Watcher is set to active when loop is enabled, and loop will run while it's true */
    private var watcherActive = false

    /** Job object for instances directory watcher */
    private var watcherJob: Job? = null

    /** Job for instance checker */
    private var checkerJob: Job? = null


    init {
        LOGGER.trace { "Instance Manager Starting" }
        watcherActive = true

        instMgrScope.launch {
            LOGGER.trace { "Launching DirectoryWatcher Job" }
            watcherJob = launch { runDirectoryWatcherLoop() }
            LOGGER.trace { "Launching InstanceChecker Job" }
            checkerJob = launch { runInstanceCheckerLoop() }
        }.invokeOnCompletion { close() }

        LOGGER.trace { "Instance Manager Started" }
    }


    /**
     * Process an Instance file and add to instancesMap
     * @param eventPath Fully Resolved Path of an Instance File
     */
    private suspend fun processInstanceFileCreateModify(eventPath: Path) = withContext(instanceReaderDispatcher) {

        try {
            val eventFilename = eventPath.name

            LOGGER.trace { "processInstanceFile: File Name > $eventPath" }

            //Get Contents of file - not bothering with a buffered reader since it's a small file, and we're loading the whole thing
            val contents = FileReader(eventPath.toFile()).use { it.readText() }

            LOGGER.trace { "processInstanceFile: Done reading $eventFilename - ${contents.length} Chars" }

            try {

                if (contents.isNotBlank() && contents.length > 2) {

                    val vtInstance = Json.decodeFromString<VtInstance>(contents)

                    //Check - make sure values are filled before proceeding
                    check(vtInstance.time > 0) { "vtInstance missing timestamp" }
                    check(vtInstance.time >= getUnixTime() - READ_TIMEOUT_SEC) { "vtInstance read timeout expired" }

                    check(vtInstance.name.isNotBlank()) { "vtInstance missing name" }
                    // Length check to handle bug in mine 2.0a where server can be ":0" when ws server from on to off
                    check(vtInstance.server.isNotBlank() && vtInstance.server.length > 3) { "vtInstance missing server" }

                    LOGGER.trace { "processInstanceFile: $eventFilename Json - $vtInstance" }

                    //Get Instance ID Object from Map, or Create if new
                    val instanceID = instancesIDMap.getOrPut(eventFilename) { InstanceID(eventFilename) }

                    LOGGER.trace { "processInstanceFile: instancesMapMutex - Lock Waiting" }

                    instancesMapMutex.withLock {
                        /* Sync Block Start */
                        LOGGER.trace { "processInstanceFile: instancesMapMutex - Lock Acquired" }

                        // Check if name is in map - if missing, create new Instance, add Instance ID and add to Map, set newInstance to True
                        var newInstance = false
                        val existingInstance: Instance =
                            instancesMap.getOrPut(instanceID) {
                                Instance(instanceID, vtInstance.name, vtInstance.server).also {
                                    newInstance = true
                                }
                            }

                        existingInstance.fileLastModified = vtInstance.time

                        //Check/Update values
                        if (!newInstance) {
                            //Existing instance - compare and update
                            LOGGER.trace { "processInstanceFile: $eventFilename existing instance - $existingInstance" }
                            if (existingInstance.name != vtInstance.name || existingInstance.server != vtInstance.server) {

                                //Important Value Changed, this should trigger a change event
                                if (existingInstance.name != vtInstance.name) {
                                    LOGGER.trace { "processInstanceFile: name change ${existingInstance.name} -> ${vtInstance.name} " }
                                }

                                if (existingInstance.server != vtInstance.server) {
                                    LOGGER.trace { "processInstanceFile: server change ${existingInstance.server} -> ${vtInstance.server} " }
                                }

                                val newInstanceObj =
                                    Instance(instanceID, vtInstance.name, vtInstance.server, vtInstance.time)
                                instancesMap[instanceID] = newInstanceObj

                                LOGGER.debug { "processInstanceFile: Existing instance updated - $existingInstance" }

                                instanceEventListener.onInstanceChange(newInstanceObj, existingInstance)
                            }
                        } else {
                            LOGGER.debug { "processInstanceFile: New instance added - $existingInstance" }
                            instanceEventListener.onInstanceStart(existingInstance)
                        }

                    }

                }
            } catch (ex: SerializationException) {
                /* Sometimes happens when the file happens to be read when it's still being written */
                LOGGER.debug { "processInstanceFile: $eventFilename content - $contents - $ex" }
            } catch (ex: IllegalArgumentException) {
                // Not valid instance of VtInstance - could be a newer/non-mini version of Veadotube
                LOGGER.debug { "processInstanceFile: $eventFilename content - $contents - $ex" }
            } catch (ex: IllegalStateException) {
                // Missing vtInstance value, etc.
                LOGGER.debug { "processInstanceFile: $eventFilename content - $contents - $ex" }
            }
        } catch (ex: IOException) {
            LOGGER.debug { "processInstanceFile: $ex" }
        }
    }


    private suspend fun runDirectoryWatcherLoop() = withContext(instanceReaderDispatcher) {
        LOGGER.trace { "DirectoryWatcher: Loop Start" }

        //Instance File Watcher Service
        val instDirWatchService: WatchService = FileSystems.getDefault().newWatchService()

        //Start Watching Directory
        val instDirPathKey: WatchKey =
            dirInstances.register(instDirWatchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)

        try {
            LOGGER.trace { "DirectoryWatcher: Watcher Loop Start" }
            while (watcherActive && isActive) {
                val loopStartTime = Instant.now().epochSecond
                val instDirLoopKey: WatchKey = instDirWatchService.take()
                var filesFound = false

                //Poll for changes in instances folder - does not block if not files found
                instDirLoopKey.pollEvents().asFlow().onStart {
                    LOGGER.trace { "DirectoryWatcher: Poll File Events Start" }
                }.onCompletion { LOGGER.trace { "DirectoryWatcher: Polling File Events" } }
                    .filterNot { event -> event.kind() === OVERFLOW }
                    .transform { event ->
                        // Resolve the filename from context of the event.
                        val eventPath: Path = dirInstances.resolve(event.context() as Path)

                        if (event.kind() === ENTRY_DELETE) {
                            // We won't do anything, there's a timeout for instances
                            LOGGER.trace { "DirectoryWatcher: File Deleted: ${eventPath.name}" }
                        } else {
                            // For 'Create' or 'Modify' Event - Launches coroutine to get and process for each file
                            LOGGER.trace { "DirectoryWatcher: File Created or Modified: ${eventPath.name}" }
                            filesFound = true
                            //Emit Path for processing
                            emit(eventPath)
                        }
                    }
                    .collect { path ->
                        //Process File
                        processInstanceFileCreateModify(path)
                    }

                // Reset key for next loop, if it fails loop ends
                check(instDirLoopKey.reset()) { "Folder Watch Key no longer Valid" }

                //Calculate loop time and delay before next loop
                val loopTimeSeconds = Instant.now().epochSecond - loopStartTime
                var delayTimeMSec =
                    READ_LOOP_DELAY_MAX_MS - (loopTimeSeconds * 1000) //Start time minus End Time = Seconds Passed
                when {
                    (delayTimeMSec < READ_LOOP_DELAY_MIN_MS) -> delayTimeMSec = READ_LOOP_DELAY_MIN_MS //min wait
                    (delayTimeMSec > READ_LOOP_DELAY_MAX_MS) -> delayTimeMSec = READ_LOOP_DELAY_MAX_MS //max wait
                }

                // If no files processed this loop, double wait time
                if (!filesFound) delayTimeMSec *= 2

                LOGGER.trace { "DirectoryWatcher: Loop took $loopTimeSeconds Seconds, Delaying ${delayTimeMSec / 1000f} Seconds before next check" }
                delay(delayTimeMSec)
            }
            LOGGER.trace { "DirectoryWatcher: Watcher Loop Ended" }
        } catch (ex: ClosedWatchServiceException) {
            LOGGER.trace { "DirectoryWatcher: WatchService Closed with ${ex.message}" }
        } catch (ex: Exception) {
            LOGGER.debug { "DirectoryWatcher: Exception in DirectoryWatcher: ${ex.message}" }
        } finally {
            LOGGER.trace { "DirectoryWatcher: Finally Cleanup" }
            watcherActive = false

            //Cleanup
            instDirPathKey.cancel()
            instDirWatchService.close()
        }

        LOGGER.trace { "DirectoryWatcher: Loop Closed" }
    }


    private suspend fun runInstanceCheckerLoop() = withContext(instanceCheckerDispatcher) {
        LOGGER.trace { "InstanceChecker: Start" }

        try {
            //Delay before loop
            delay(READ_LOOP_DELAY_MAX_MS)

            while (watcherActive && isActive) {
                val loopStartTime = Instant.now().epochSecond


                LOGGER.trace { "InstanceChecker: instancesMapMutex - Lock Waiting" }

                instancesMapMutex.withLock {
                    /* Sync Block Start */
                    LOGGER.trace { "InstanceChecker: instancesMapMutex - Lock Acquired" }

                    if (instancesMap.isNotEmpty()) {

                        val instMapIterator = instancesMap.iterator()

                        // Find Instances to Remove and process
                        for (instanceEntry in instMapIterator) {
                            if (instanceEntry.value.fileLastModified < getUnixTime() - READ_TIMEOUT_SEC) {
                                //Instance has aged out without file refresh, trigger end and remove from map
                                instanceEventListener.onInstanceEnd(instanceEntry.value.id)
                                instMapIterator.remove()
                            }
                        }

                    }

                    /* Sync Block End */
                }


                val loopTimeSeconds = Instant.now().epochSecond - loopStartTime
                val delayTimeMSec =
                    if (instancesMap.isNotEmpty()) READ_LOOP_DELAY_MAX_MS else READ_LOOP_DELAY_MAX_MS * 2 // If no instances are in the map, double wait time
                LOGGER.trace { "InstanceChecker: Loop took $loopTimeSeconds Seconds, Delaying ${delayTimeMSec / 1000f} Seconds before next check" }
                delay(delayTimeMSec)
            }

        } finally {
            watcherActive = false

            //Cleanup
            instancesMapMutex.withLock {
                for (instance in instancesMap.values) {
                    instanceEventListener.onInstanceEnd(instance.id)
                }

                instancesMap.clear()
            }
            LOGGER.trace { "InstanceChecker: Cleanup Done" }
        }

    }


    /** Function to clean up after instMgrJob */
    private fun cleanup() {
        LOGGER.trace { "Instance Manager Cleanup Start" }
        watcherActive = false

        LOGGER.trace { "Closing DirectoryWatcher Job" }
        runCatching { watcherJob?.cancel("Instances Manager is Closing") }
        watcherJob = null

        LOGGER.trace { "Closing InstanceChecker Job" }
        runCatching { checkerJob?.cancel("Instances Manager is Closing") }
        checkerJob = null

        LOGGER.trace { "Instance Manager Cleanup Done" }
    }

    override fun close() {
        if (instMgrJob.isCancelled) {
            return
        }
        watcherActive = false

        runCatching { instMgrJob.cancel("Instances Manager is Closing") }
    }


    /**
     * Returns [Instance] Object from Map that matches [InstanceID] Object
     */
    fun getInstance(id: InstanceID): Instance? {
        var inst: Instance?
        runBlocking {
            instancesMapMutex.withLock {
                inst = instancesMap[id]
            }
        }

        return inst
    }

    /**
     * Removes [Instance] Object from Map that matches [InstanceID] Object
     *
     * This means next instance check it should be either cleared or restarted
     */
    fun markInstanceFailed(id: InstanceID) {

        runBlocking {
            instancesMapMutex.withLock {
                LOGGER.trace { "markInstanceFailed: Marking $id as failed" }
                instancesMap.remove(id)
                instanceEventListener.onInstanceEnd(id)
            }
        }

    }

}