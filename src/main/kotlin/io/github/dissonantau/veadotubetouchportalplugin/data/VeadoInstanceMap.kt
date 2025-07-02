package io.github.dissonantau.veadotubetouchportalplugin.data

import io.github.dissonantau.bleatkan.connection.Connection
import io.github.dissonantau.bleatkan.instance.Instance
import io.github.dissonantau.bleatkan.instance.InstanceID
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.collections4.bidimap.TreeBidiMap
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

class VeadoInstanceMap {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    private val lock = ReentrantReadWriteLock()
    private val lockWrite = lock.writeLock()

    @Suppress("MemberVisibilityCanBePrivate")
    val lockRead: ReentrantReadWriteLock.ReadLock = lock.readLock()


    /**
     * Veadotube Instance Collection
     *
     * String = [InstanceID.toString] Value
     *
     * Instance = Map of [Instance.instanceConnectionID] vs Instance Objects
     *
     * Should be run in a Synchronized context
     */
    private val instanceHashMap = HashMap<String, LinkedHashMap<String, Instance>>()


    /** instanceMap.isEmpty() */
    fun isEmpty() = lock.read { instanceHashMap.isEmpty() }


    /**
     * Executes the given [action] under the read lock of this [VeadoInstanceMap].
     * @return the return value of the action.
     */
    fun <T> runWithReadLock(action: () -> T): T {
        return lockRead.withLock { action() }
    }


    /**
     * Stable Instance Number -> InstanceID Map
     *
     * Key = Int - Instance Number
     *
     * Value = [InstanceID]
     *
     * Instance Number is filled from 1, lowest to highest.
     *
     * Instance Number is fixed as long as:
     * - Instance has had the Websocket server enabled at least once
     * - Instance isn't closed
     *
     * When an Instance Websocket server is disabled, the [InstanceID] isn't removed from the list.
     * Re-enabling the Websocket server then allows the same number to be used.
     *
     * When an Instance is closed, the number is freed and available for reuse.
     *
     * For example:
     * - Instance A starts > Becomes #1
     * - Instance B starts > Becomes #2
     * - Instance A websocket is disabled and re-enabled > Stays as #1
     * - Instance A closes > #1 is freed (null)
     * - Instance C Starts > Becomes #1
     *
     */
    private val instanceNumberMaps: HashMap<String, LinkedHashMap<Int, InstanceID?>> = HashMap()


    /**
     * Returns a copy of the Stable Instance Number Map
     *
     * [instanceType] is the type of veadotube instance ('mini', 'veado' (full), etc.)
     *
     * Key = Int - Instance Number
     *
     * Value = [InstanceID]
     *
     * Instance Number is filled from 1, lowest to highest.
     *
     * Instance Number is fixed as long as:
     * - Instance has had the Websocket server enabled at least once
     * - Instance isn't closed
     *
     * When an Instance Websocket server is disabled, the [InstanceID] isn't removed from the list.
     * Re-enabling the Websocket server then allows the same number to be used.
     *
     * When an Instance is closed, the number is freed and made available for reuse.
     *
     * For example:
     * - Instance A starts > Becomes #1
     * - Instance B starts > Becomes #2
     * - Instance A websocket is disabled and re-enabled > Stays as #1
     * - Instance A closes > #1 is freed (null)
     * - Instance C Starts > Becomes #1
     *
     */
    fun getInstanceNumberMap(instanceType: String): Map<Int, InstanceID?> {
        require(instanceType.isNotEmpty()) { "instanceType must not be empty" }

        lock.read {
            val instanceNumberMap = instanceNumberMaps[instanceType] ?: return emptyMap()
            return instanceNumberMap.toMap()
        }
    }

    /**
     * Gets Instance Number of Instance ID
     *
     * Returns -1 if none found
     */
    fun getInstanceNumber(instanceID: InstanceID): Int {
        val instanceType: String = instanceID.type
        lock.read {
            // Search map of matching value, return ley (Stable Instance Number)
            instanceNumberMaps[instanceType]?.forEach { if (it.value == instanceID) return it.key }

            return -1
        }
    }


    /**
     * Returns a map of Stable Instance Numbers and related Connections
     *
     * Only returns Instance Numbers with a matching Connection
     *
     * @see getInstanceNumberMap
     */
    fun getInstanceConnectionNumberMap(instanceType: String): Map<Int, Connection> {
        require(instanceType.isNotEmpty()) { "instanceType must not be empty" }

        lock.read {
            val instanceNumberMap = instanceNumberMaps[instanceType] ?: return emptyMap()

            val connectionMap = LinkedHashMap<Int, Connection>()
            instanceNumberMap.forEach { mapEntry ->
                mapEntry.value
                    ?.let { collInstConnections[it.toString()]?.values?.lastOrNull() }
                    ?.let { connectionMap[mapEntry.key] = it }
            }

            return connectionMap
        }
    }


    /**
     * Returns a map of Stable Instance Numbers and related Connections
     *
     * Returns all Instance Numbers, even if there's no matching connection
     *
     * @see getInstanceNumberMap
     */
    fun getInstanceConnectionNumberMapAll(instanceType: String): Map<Int, Connection?> {
        require(instanceType.isNotEmpty()) { "instanceType must not be empty" }

        lock.read {
            val instanceNumberMap = instanceNumberMaps[instanceType] ?: return emptyMap()

            val connectionMap = LinkedHashMap<Int, Connection?>()
            instanceNumberMap.forEach { mapEntry ->
                connectionMap[mapEntry.key] = mapEntry.value
                    .let { collInstConnections[it.toString()]?.values?.lastOrNull() }
            }

            return connectionMap
        }
    }

    /**
     * Adds InstanceID to Stable Instance Number Map
     *
     * Iterates through Map and sets first match to provided Instance ID
     *
     * Returns Number Assigned
     */
    private fun addInstanceNumberMap(instanceID: InstanceID): Int {
        val instanceNumberMap = instanceNumberMaps.getOrPut(
            key = instanceID.type,
            defaultValue = { LinkedHashMap() }
        )
        if (instanceNumberMap.isEmpty()) {
            instanceNumberMap[1] = instanceID
            return 1
        } else {
            var rowNum = 1
            var firstEmptyRow = -1
            // Iterate through all entries, picking the first empty row or returning if an exact match of given InstanceID is found
            for (row in instanceNumberMap.iterator()) {

                // If first empty not already found, and row is null, we'll use it as the number for the new instanceID
                // If row key doesn't match rowNum, a row must have been deleted - treat it as null
                if (firstEmptyRow <= 0 && (row.value == null || row.key != rowNum)) {
                    firstEmptyRow = rowNum
                }

                // If a match of instance is found, return
                if (row.value == instanceID) return rowNum

                rowNum++
            }

            // Add instanceID to map
            if (firstEmptyRow > 0) {
                // Set using firstEmpty as key
                instanceNumberMap[firstEmptyRow] = instanceID
                return firstEmptyRow
            } else {
                // If first empty is not found, use rowNum for new entry
                instanceNumberMap[rowNum] = instanceID
                return rowNum
            }
        }
    }

    /**
     * Removes InstanceID from Map
     *
     * Iterates through Map and sets first match to null
     *
     * Returns true if match was found
     */
    private fun removeInstanceNumberMap(instanceID: InstanceID): Boolean {
        val instanceNumberMap = instanceNumberMaps[instanceID.type]
        if (instanceNumberMap.isNullOrEmpty()) return false
        else {
            // Iterate through all entries, if matching row is found, set null and return
            for (row in instanceNumberMap.iterator()) {
                // If a match of instance is found, set to null and return
                if (row.value == instanceID) {
                    row.setValue(null)
                    return true
                }

            }
            return false
        }
    }


    /**
     * Veadotube Mini Sorted Set
     *
     * Instance = Set of [Instance] sorted by [Instance.id] ([Instance.COMPARATOR_INSTANCE_BY_ID])
     *
     * Should be run in a Synchronized context
     *
     * Is sorted by Instance Start Timestamp
     */
    private var instanceMiniSortedSet: SortedSet<Instance> = TreeSet(Instance.COMPARATOR_INSTANCE_BY_ID)


    /** Gets Copy of Instances in Order (by Age, oldest first)*/
    fun getMiniInstanceList(): Set<Instance> =
        lock.read {
            instanceMiniSortedSet.toSet()
        }


    /** Checks if Instance List is empty */
    fun getMiniInstanceListIsEmpty(): Boolean =
        lock.read {
            instanceMiniSortedSet.isEmpty()
        }


    /** Gets Set of Connection, ordered by Instance Order (Age, oldest first)*/
    fun getMiniInstanceConnectionList(): Set<Connection> =
        lock.read {
            instanceMiniSortedSet.mapNotNullTo(mutableSetOf()) { collInstConnections[it.id.toString()]?.values?.last() }
        }


    /**
     * Veadotube Connection Collection
     *
     * String - [InstanceID.toString] Value
     *
     * Connection - Connection Map [Connection].[Instance.title] vs [Connection]
     *
     * Should be run in a Synchronized context
     */
    private val collInstConnections = HashMap<String, LinkedHashMap<String, Connection>>()


    fun getConnectionsList(): List<Connection> =
        lock.read {
            collInstConnections.flatMap { it.value.values }
        }

    /**
     * Find the Latest/Newest Connection by ID
     */
    fun getConnectionByInstanceID(id: InstanceID): Connection? {
        lock.read {
            return collInstConnections[id.toString()]?.values?.last()
        }
    }

    /**
     * Find the Oldest Connection by ID
     */
    fun getConnectionOldestByInstanceID(id: InstanceID): Connection? {
        lock.read {
            return collInstConnections[id.toString()]?.values?.first()
        }
    }

    fun getMiniConnectionOldestByInstanceID(id: InstanceID): Connection? =
        lock.read {
            instanceMiniSortedSet.firstOrNull()?.let { collInstConnections[it.id.toString()]?.values?.last() }
        }

    fun getMiniConnectionNewestByInstanceID(id: InstanceID): Connection? =
        lock.read {
            instanceMiniSortedSet.lastOrNull()?.let { collInstConnections[it.id.toString()]?.values?.last() }
        }

    /**
     * Returns Count of all Connections
     */
    fun getConnectionCount(): Int {
        lock.read {
            return collInstConnections.values.fold(0) { acc, linkedHashMap -> acc + linkedHashMap.count() }
        }
    }


    /**
     * Map of [Connection]s with related [VeadoConnectionData]
     *
     * Contains data collected from Connection
     *
     * Work on held objects with should be synchronized with the [Connection] to prevent issues.
     *
     */
    private val collConnectionData = HashMap<Connection, VeadoConnectionData>()

    /**
     * Veadotube Instance Collection
     *
     * String = [InstanceID.toString] Value
     *
     * String = [Connection].[Instance.title] Value
     *
     * Reverse is at [collInstanceTitleID]
     *
     * Should be run in a Synchronized context
     */
    private val collInstanceIDTitle = TreeBidiMap<String, String>()

    /**
     * Veadotube Instance Collection - Reverse of [collInstanceIDTitle]
     *
     * String = [Connection].[Instance.title] Value
     *
     * String = [InstanceID.toString] Value
     *
     * Should be run in a Synchronized context
     */
    private val collInstanceTitleID = collInstanceIDTitle.inverseBidiMap()


    fun onInstanceDetected(instance: Instance) {
        val instanceID = instance.id
        val instanceIDString = instanceID.toString()

        lock.write {
            LOGGER.trace { "onInstanceOpen: Instance $instanceID" }
            // Add to instanceMap
            instanceHashMap.getOrPut(instanceIDString) { LinkedHashMap() }

        }
    }


    /**
     * Add new Instance to Collection
     *
     * Returns:
     * * 0 if successful and no instance already exists
     * * 1 if instance already exists and only has a Window Title Mismatch
     * * 2 if instance already exists and existing entry Window Title matches (Existing Instance isn't replaced)
     */
    fun onInstanceStart(instance: Instance): Int {
        val instanceTitle = instance.title
        val instanceConnectionID = instance.instanceConnectionID
        val instanceID = instance.id
        val instanceIDString = instance.id.toString()
        val instanceType = instance.id.type

        lock.write {
            LOGGER.trace { "onInstanceServerStart: Instance: $instance}" }
            // Add to instanceMap
            val map = instanceHashMap[instanceIDString] ?: return 3

            val putResult = map.putIfAbsent(instanceConnectionID, instance)

            if (putResult != null) {
                // Instance ID matches existing
                if (putResult.title == instance.title) {
                    // Same Windows Title
                    LOGGER.trace { "onInstanceServerStart: New Instance Matches Existing $instanceIDString '$instanceTitle'" }
                    return 2
                }
                // Title Doesn't Match
                LOGGER.trace { "onInstanceServerStart: New Instance Matches Existing, Different Title $instanceIDString '$instanceTitle', Existing '${putResult.title}'" }
                return 1
            }

            // Add to BiDi Map
            LOGGER.trace { "onInstanceServerStart: ID to Title Map $instanceIDString $instanceTitle" }
            val prev = collInstanceTitleID.putIfAbsent(instanceTitle, instanceIDString)

            if (prev != null) {
                // Previous Value, can't map. Likely another instance has the same Title
                LOGGER.warn { "Duplicate Title for instance with title '$instanceTitle' - Change the Window title to allow Touch Portal to control it" }
            }

            //Add to Mini Instance Set
            if (instanceType == "mini") {
                instanceMiniSortedSet.add(instance)
            }

            return 0
        }
    }


    /**
     * Add new Connection to Collection
     *
     * Returns:
     * * 0 on Success
     * * -1 if instance hasn't been added to map
     */
    fun onConnectionStart(connection: Connection): Int {
        val instance = connection.instance
        val instanceTitle = instance.title
        //val instanceConnectionID = instance.instanceConnectionID
        val instanceID = instance.id.toString()
        val instanceType = instance.id.type

        lock.write {
            LOGGER.trace { "onConnectionStart: Instance $instance}" }
            // Get instance map, return -1 if it doesn't exist
            if (!instanceHashMap.containsKey(instanceID)) return -1

            //Add to Mini Instance Set
            if (instanceType == "mini") {
                instanceMiniSortedSet.add(instance)
            }

            // Create Connection and add to Connection Collection
            LOGGER.trace { "onConnectionStart: Connection to Map $instanceID ${connection.connUri}" }
            val connMap = collInstConnections.getOrPut(instanceID) { LinkedHashMap() }
            connMap[instanceTitle] = connection

            return 0
        }
    }


    /**
     * Remove Connection and Data linked to an Instance
     *
     * Uses the Instance Title to match connection for removal
     *
     * Returns Connection related to Instance for closing and external cleanup
     *
     */
    fun instanceRemoveConnection(instance: Instance): Pair<Connection?, VeadoConnectionData?> {
        val instanceTitle = instance.title
        val instanceServer = instance.server
        val instanceConnectionID = instance.instanceConnectionID
        val instanceID = instance.id.toString()

        LOGGER.trace { "instanceCloseAndCleanup: Close and cleanup instance $instanceServer $instanceTitle" }

        lock.write {
            // Get list of connections tied to the instanceID
            val removedConnectionCandidates = collInstConnections[instanceID] ?: return Pair(null, null)

            // Remove connection from Collection and make sure connection is closed
            val removedConnection = removedConnectionCandidates[instanceTitle]

            val connData = removedConnection?.let {
                LOGGER.trace { "instanceRemoveConnection: Close Connection ${it.instance.title} ${it.server} ${it.name}" }

                //Cleanup States
                removedConnectionCandidates.remove(instanceTitle)
                collConnectionData.remove(it)
            }

            LOGGER.trace { "onConnectionStart: Remove from ID to Title Map $instanceID $instanceTitle" }
            collInstanceIDTitle.remove(instanceID)

            // Remove from Maps
            LOGGER.trace { "instanceRemoveConnection: Remove Instance from Map $instanceConnectionID" }
            instanceHashMap[instanceID]?.remove(instanceConnectionID)?.also {
                if (it.id.type == "mini") instanceMiniSortedSet.remove(it)
            }

            return Pair(removedConnection, connData)
        }
    }


    /**
     * Remove Instance/Connections linked to an InstanceID
     *
     * Returns Connections related to InstanceID for closing and cleanup
     */
    fun instanceRemove(id: InstanceID): Collection<Connection>? {
        val instanceID = id.toString()

        LOGGER.trace { "onInstanceClose: Remove and cleanup instance $id" }

        lock.write {
            // Remove connection from Collection and make sure connection is closed
            val removedConnections: MutableCollection<Connection>? = collInstConnections.remove(instanceID)?.values

            removedConnections?.forEach { connection ->
                LOGGER.trace { "onInstanceClose: Remove Connection State Data ${connection.server} ${connection.name}" }

                // Cleanup States Info
                collConnectionData.remove(connection)
            }

            // Remove Instance(s) from Map
            instanceHashMap.remove(instanceID)?.values?.onEach { instance ->
                if (instance.id.type == "mini") instanceMiniSortedSet.remove(instance)
            }

            removeInstanceNumberMap(id)

            collInstanceIDTitle.remove(instanceID)

            return removedConnections
        }
    }


    fun cleanupConnectionStates(connection: Connection) {
        val instance = connection.instance
        val instanceTitle = instance.title
        val instanceConnectionID = instance.instanceConnectionID
        val instanceID = instance.id.toString()

        LOGGER.trace { "cleanupConnectionStates: Cleanup and remove $instanceID $instanceConnectionID" }

        lock.write {
            // Remove Connection
            collInstConnections[instanceID]?.remove(instanceTitle)

            // Cleanup States Info
            collConnectionData.remove(connection)
        }
    }


    /** Returns Oldest Mini Instance or null if it's empty */
    fun getOldestMiniInstance(): Instance? {
        lock.read {
            return if (instanceMiniSortedSet.isEmpty()) null
            else instanceMiniSortedSet.first()
        }
    }


    fun getConnectionInstanceData(connection: Connection): VeadoConnectionData? {
        lock.read {
            return collConnectionData[connection]
        }
    }

    /**
     * Update Title related mappings of active Instance
     */
    fun updateInstanceName(instance: Instance, oldValue: String): Pair<Connection?, List<String>?> {
        val instanceID = instance.id.toString()

        lock.write {
            // Update Linked Hash Map - Remove Old Title Key, add connection against new key if it existed
            val connMap = collInstConnections[instanceID]

            val connection = connMap?.remove(oldValue)

            val titles =
                if (connection != null) {
                    // Re-add connection to map with new title
                    connMap[instance.title] = connection
                    // Update BiDI Map
                    collInstanceIDTitle[instanceID] = connection.instance.title

                    //Trigger Title Refresh
                    collConnectionData[connection]?.refreshInstanceTitle()
                } else null

            return Pair(connection, titles)
        }
    }


    fun isConnectionInCollection(connection: Connection): Boolean {
        val instance = connection.instance
        val instanceID = instance.id.toString()
        lock.read {
            return collInstConnections[instanceID]?.containsValue(connection) ?: false
        }
    }


    /**
     * Add new Connection to Collection
     *
     * Returns list of connections updated, including any that have had Instance numbers updated
     */
    fun onConnectionActivate(connection: Connection): VeadoConnectionData? {
        val instance = connection.instance
        val instanceID = instance.id.toString()
        val instanceType = instance.id.type

        lock.write {
            LOGGER.trace { "onConnectionActivate: Instance $instance Connection ${connection.connUri}" }

            // Add to map
            val linked =
                collInstConnections.getOrPut(instanceID) { LinkedHashMap() }

            linked[instance.title] = connection

            // Add to Map if it hasn't been added already
            val putNumberResult = addInstanceNumberMap(instance.id)

            // Create Connection Data Holder and add to collection
            val connData = collConnectionData.getOrPut(connection) { VeadoConnectionData(connection) }

            // Update instance IDs
            when (instanceType) {
                "mini" -> {
                    if (putNumberResult == -1) {
                        LOGGER.warn { "Connection can't be mapped to Stable Instance Number" }
                    }
                    connData.setInstanceNumber(putNumberResult)

                    return connData
                }

                "veado" -> {
                    //TODO
                }

                else -> {
                    LOGGER.warn { "Unknown Instance Type: $instanceType" }
                }
            }

        }
        return null
    }

    /** Updates Instance IDs - returns list of connections with updated Numbers*/
    private fun updateMiniInstanceNumbers(): List<VeadoConnectionData> {
        val updated = mutableListOf<VeadoConnectionData>()
        lock.write {
            var count = 1
            getMiniInstanceConnectionList().forEach { connection ->
                collConnectionData[connection]?.let { connData ->
                    // Get current number, if it doesn't match expected, update and add to 'update' list
                    val current = connData.getInstanceNumber()
                    if (current != count) {
                        connData.setInstanceNumber(count++)
                        updated.add(connData)
                    }
                }
            }
            return updated
        }
    }

}


enum class VeadoInstanceMapTypes(lowercase: String) {
    MINI("mini"),
    FULL("full");

    companion object {
        fun valueOfCaseInsensitive(name: String): VeadoInstanceMapTypes =
            valueOf(name.uppercase())
    }

}