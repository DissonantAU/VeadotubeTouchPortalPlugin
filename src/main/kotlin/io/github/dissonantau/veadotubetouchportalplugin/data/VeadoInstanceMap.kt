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

enum class VeadoInstanceMapTypes(lowercase: String) {
    MINI("mini"),
    FULL("full");

    companion object {
        fun valueOfCaseInsensitive(name: String): VeadoInstanceMapTypes =
            VeadoInstanceMapTypes.valueOf(name.uppercase())
    }

}

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
     * Veadotube Instance Collection
     *
     * Instance = Set of [Instance] sorted by [Instance.id]
     *
     * Should be run in a Synchronized context
     *
     * Is sorted by Instance Start Timestamp
     */
    private var instanceMiniSortedSet: SortedSet<Instance> = TreeSet(Instance.COMPARATOR_INSTANCE_BY_ID)


    fun getMiniInstanceList(): Set<Instance> =
        lock.read {
            instanceMiniSortedSet.toSet()
        }

    fun getMiniInstanceListIsEmpty(): Boolean =
        lock.read {
            instanceMiniSortedSet.isEmpty()
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
     * Find the Latest Connection buy ID
     */
    fun getConnectionByInstanceID(id: InstanceID): Connection? {
        lock.read {
            return collInstConnections[id.toString()]?.values?.last()
        }
    }

    /**
     * Find the Oldest Connection buy ID
     */
    fun getConnectionOldestByInstanceID(id: InstanceID): Connection? {
        lock.read {
            return collInstConnections[id.toString()]?.values?.last()
        }
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


    /**
     * Add new Instance to Collection
     */
    fun onInstanceStart(instance: Instance) {
        val instanceTitle = instance.title
        val instanceConnectionID = instance.instanceConnectionID
        val instanceID = instance.id.toString()

        lock.write {
            LOGGER.trace { "onInstanceStart: Instance: $instance}" }
            // Add to instanceMap
            val map = instanceHashMap.getOrPut(instanceID) { LinkedHashMap() }
            map[instanceConnectionID] = instance

            // Add to BiDI Map
            LOGGER.trace { "onInstanceStart: ID to Title Map $instanceID $instanceTitle" }
            collInstanceIDTitle[instanceID] = instanceTitle

            //Add to Mini Instance Set
            if (instance.id.type == "mini") {
                instanceMiniSortedSet.add(instance)
            }
        }
    }


    /**
     * Add new Connection to Collection
     */
    fun onConnectionStart(connection: Connection) {
        val instance = connection.instance
        val instanceTitle = instance.title
        val instanceConnectionID = instance.instanceConnectionID
        val instanceID = instance.id.toString()

        lock.write {
            LOGGER.trace { "onConnectionStart: Instance $instance}" }
            // Add to instanceMap
            val map = instanceHashMap.getOrPut(instanceID) { LinkedHashMap() }
            map[instanceConnectionID] = instance

            // Add to BiDi Map
            LOGGER.trace { "onConnectionStart: ID to Title Map $instanceID $instanceTitle" }
            collInstanceIDTitle[instanceID] = instanceTitle

            //Add to Mini Instance Set
            if (instance.id.type == "mini") {
                instanceMiniSortedSet.add(instance)
            }

            // Create Connection and add to Connection Collection
            LOGGER.trace { "onConnectionStart: Connection to Map $instanceID ${connection.connUri}" }
            val connMap = collInstConnections.getOrPut(instanceID) { LinkedHashMap() }
            connMap[instanceTitle] = connection
        }
    }


    /**
     * Remove Connections linked to an Instance
     *
     * Returns Connection related to Instance for closing and external cleanup
     */
    fun instanceConnectionCloseAndCleanup(instance: Instance): Connection? {
        val instanceTitle = instance.title
        val instanceServer = instance.server
        val instanceConnectionID = instance.instanceConnectionID
        val instanceID = instance.id.toString()

        LOGGER.trace { "instanceCloseAndCleanup: Close and cleanup instance $instanceServer $instanceTitle " }

        var removedConnection: Connection? = null

        lock.write {
            // Remove connection from Collection and make sure connection is closed
            removedConnection = collInstConnections[instanceID]?.get(instanceTitle)

            removedConnection?.let {
                LOGGER.trace { "instanceConnectionCloseAndCleanup: Close Connection ${it.server} ${it.name}" }

                //Cleanup States
                collConnectionData.remove(it)
            }


            LOGGER.trace { "onConnectionStart: Remove from ID to Title Map $instanceID $instanceTitle" }
            collInstanceIDTitle.remove(instanceID)

            // Remove from Maps
            LOGGER.trace { "instanceConnectionCloseAndCleanup: Remove Instance from Map $instanceConnectionID" }
            instanceHashMap[instanceID]?.remove(instanceConnectionID)?.also {
                if (it.id.type == "mini") instanceMiniSortedSet.remove(it)
            }

        }
        return removedConnection
    }


    /**
     * Remove Instance/Connections linked to an InstanceID
     *
     * Returns Connections related to InstanceID for closing and cleanup
     */
    fun instanceIdRemove(id: InstanceID): MutableCollection<Connection>? {
        val instanceID = id.toString()

        LOGGER.trace { "instanceIdRemove: Remove and cleanup instance $id " }
        var removedConnections: MutableCollection<Connection>? = null

        lock.write {
            // Remove connection from Collection and make sure connection is closed
            removedConnections = collInstConnections.remove(instanceID)?.values

            removedConnections?.forEach { connection ->
                LOGGER.trace { "instanceIdRemove: Remove Connection State Data ${connection.server} ${connection.name}" }

                // Cleanup States Info
                collConnectionData.remove(connection)
            }

            // Remove Instance(s) from Map
            instanceHashMap.remove(instanceID)?.values?.onEach { instance ->
                if (instance.id.type == "mini") instanceMiniSortedSet.remove(instance)
            }

            collInstanceIDTitle.remove(instanceID)
        }

        return removedConnections
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


    fun getConnectionData(connection: Connection): VeadoConnectionData? {
        lock.read {
            return collConnectionData[connection]
        }
    }

    fun updateInstanceName(instance: Instance, oldValue: String) {
        val instanceID = instance.id.toString()

        // Update Linked Hash Map - Remove Old Title Key, add connection against new key if it existed
        val connMap = collInstConnections.getOrPut(instanceID) { LinkedHashMap() }

        connMap.remove(oldValue)?.let { connection ->
            connMap.put(instance.title, connection)

        }

    }


    fun isConnectionInCollection(connection: Connection): Boolean {
        val instance = connection.instance
        val instanceID = instance.id.toString()
        return collInstConnections[instanceID]?.containsValue(connection) ?: false
    }


    /**
     * Add new Connection to Collection
     */
    fun onConnectionActivate(connection: Connection) {
        val instance = connection.instance
        val instanceID = instance.id.toString()

        lock.write {
            LOGGER.trace { "onConnectionActivate: Instance $instance Connection ${connection.connUri}" }

            // Add to map
            val linked =
                collInstConnections.getOrPut(instanceID) { LinkedHashMap() }

            linked[instance.title] = connection

            // Create Connection Data Holder and add to collection
            collConnectionData[connection] = VeadoConnectionData(connection)
        }
    }

}