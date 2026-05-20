package io.github.dissonantau.veadotubetouchportalplugin.data

import io.github.dissonantau.bleatkan.message.*
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadPng as BleatkanStateThumbnail
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadState as BleatkanStatePeek
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadStateList as BleatkanStateList
import io.github.dissonantau.bleatkan.message.ResultMessage.ResultMessageWithPayload as BleatkanPayload
import io.github.dissonantau.bleatkan.message.ResultMessage.ResultMessageWithPayloadBoolean as BleatkanPayloadBoolean
import io.github.dissonantau.bleatkan.message.ResultMessage.ResultMessageWithPayloadNumber as BleatkanPayloadNumber
import io.github.dissonantau.bleatkan.connection.Connection
import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPluginConstants
import kotlin.collections.forEach
import kotlin.collections.toSet

@Suppress("unused", "NOTHING_TO_INLINE")
class VeadoFullConnectionData(connection: Connection, instanceNumber: Int) :
    VeadoConnectionData(connection, instanceNumber) {

    /** Instance title used during past refresh - used for comparisons */
    private var instanceTitle = ""

    /**
     * Generated State ID for veadotube's Window Title
     * - Excludes base section, used for updating State
     * - e.g. myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDTitledInstanceTitle = InstanceStateIdDescription("", "")
        private set

    /**
     * All Generated State IDs for veadotube's Nodes and relevant data
     * - e.g. myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val currentInstanceTitleStateIDs: Set<InstanceStateIdDescription>
        get() = _currentInstanceTitleStateIDs.toSet()

    private var _currentInstanceTitleStateIDs: MutableSet<InstanceStateIdDescription>

    /**
     * Generated State ID for mini Windows Title
     * - Excludes base section, used for updating State
     * - e.g. myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDNumberedInstanceTitle = InstanceStateIdDescription("", "")
        private set

    /**
     * All Generated State ID for veadotube's Nodes and relevant data
     * - e.g. myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val currentInstanceNumberedStateIDs: Set<InstanceStateIdDescription>
        get() = _currentInstanceNumberedStateIDs.toSet()

    private var _currentInstanceNumberedStateIDs: MutableSet<InstanceStateIdDescription>

    /**
     * - Inner Key = node ID
     * - Inner Value = Node Data Object
     */
    private val nodesStateEventsMap: MutableMap<String, VeadoStateNodeData>

    /**
     * - Inner Key = node ID
     * - Inner Value = Node Data Object
     */
    private val nodesNumberMap: MutableMap<String, VeadoNumberNodeData>

    /**
     * - Inner Key = node ID
     * - Inner Value = Node Data Object
     */
    private val nodesBooleanMap: MutableMap<String, VeadoBooleanNodeData>

    /**
     * Outer Key = Type ('stateEvents'/'boolean'/'number')
     * - Inner Key = node ID
     * - Inner Value = Node Data Object
     */
    private val nodesList: MutableMap<String, MutableMap<String, out VeadoNodeData>>

    init {
        _currentInstanceTitleStateIDs = mutableSetOf()
        _currentInstanceNumberedStateIDs = mutableSetOf()

        nodesStateEventsMap = mutableMapOf()
        nodesNumberMap = mutableMapOf()
        nodesBooleanMap = mutableMapOf()

        nodesList = mutableMapOf(
            "stateEvents" to nodesStateEventsMap,
            "number" to nodesNumberMap,
            "boolean" to nodesBooleanMap
        )

        /** Generate Instance Titles */
        refreshInstanceTitle()
        refreshInstanceNumber()
    }

    /**
     * All Instance Nodes.
     * - Only returns Node Types with at least one Node
     *
     * - Outer Key = Type ('stateEvents'/'boolean'/'number')
     * - Inner Key = node ID
     * - Inner Value = Node Data Object
     */
    val nodesAll: Map<String, Map<String, VeadoNodeData>>
        get() {
            val newMap: MutableMap<String, Map<String, VeadoNodeData>> = mutableMapOf()
            nodesList.forEach {
                if (it.value.isNotEmpty()) newMap[it.key] = it.value.toMap()
            }
            return newMap
        }

    /**
     * Map of all [VeadoStateNodeData] available
     * - Inner Key = node ID
     * - Inner Value = Node Data Object
     *
     * - IDs are unique but Names can be duplicates.
     * - Work with this should probably be synchronised using the connection obj
     */
    val getAllStateEventNodes: Map<String, VeadoStateNodeData>; get() = nodesStateEventsMap.toMap()

    /**
     * Map of all [VeadoNumberNodeData] available
     * - Inner Key = node ID
     * - Inner Value = Node Data Object
     *
     * - IDs are unique but Names can be duplicates.
     * - Work with this should probably be synchronised using the connection obj
     */
    val getAllNodesNumber: Map<String, VeadoNumberNodeData>; get() = nodesNumberMap.toMap()

    /**
     * Map of all [VeadoBooleanNodeData] available
     * - Inner Key = node ID
     * - Inner Value = Node Data Object
     *
     * - IDs are unique but Names can be duplicates.
     * - Work with this should probably be synchronised using the connection obj
     */
    val getAllNodesBoolean: Map<String, VeadoBooleanNodeData>; get() = nodesBooleanMap.toMap()

    /** Value used as first identifier for TP Numbered Dynamic States - i.e. ...MiniInstances.state.***1***.title */
    inline val tpIdPrefixNumber; get() = instanceNumber.toString()

    /** Value used as first identifier for TP Titled Dynamic States - i.e. ...MiniInstances.state.***myTitle***.title */
    inline val tpIdPrefixTitle; get() = instanceTitleSimplified

    /* StateID Strings for Full Instances */
    /**
     * Base State ID used for Dynamically creating and deleting States
     * - Excludes trailing dot (.)
     * - e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.FullInstances.state
     */
    override val baseStateID = "${VeadoTouchPluginConstants.FullInstances.ID}.state"

    ///** Map of State IDs */
    //val stateIDMap: MutableMap<String, String> = mutableMapOf()
    // TODO State Generation

    ///**
    // * Generated State ID for veadotube's Window Title
    // *
    // * Includes Base Section, used for Dynamically creating and deleting States
    // *
    // * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.FullInstances.state.myTitle.title
    // */
    //@Suppress("MemberVisibilityCanBePrivate")
    //val stateIDTitledInstanceTitleLong
    //    get() = "${baseStateID}.$stateIDTitledInstanceTitle"

    /**
     * Checks if Instance Title matches last processed, updates Title Cleaned & Simplified.
     * @return true if title was updated, false if not
     */
    fun refreshInstanceTitleCalc(): Boolean {
        if (instanceTitle != instance.title || instanceTitle.isBlank()) {
            val firstDash = instance.title.indexOf('-')
            val newInstanceTitleCleaned = if (firstDash >= 0) {
                instance.title.substring(firstDash + 1).trim()
            } else {
                "${instance.id.type}-${instance.id.process}"
            }

            if (instanceTitleCleaned != newInstanceTitleCleaned) {
                // Update Titles
                instanceTitle = instance.title
                instanceTitleCleaned = newInstanceTitleCleaned
                instanceTitleSimplified =
                    newInstanceTitleCleaned.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
                return true
            }
        }
        return false
    }

    /**
     * Updates [instanceTitleSimplified] with latest Title and fully refreshes [currentInstanceTitleStateIDs] with current Node State IDs
     * - [instanceTitleCleaned], and State IDs that use [Instance.title][io.github.dissonantau.bleatkan.instance.Instance.title] are also Updated.
     * - If nothing exists after the first Dash in the Title, "[InstanceID.type][io.github.dissonantau.bleatkan.instance.InstanceID.type]-[InstanceID.process][io.github.dissonantau.bleatkan.instance.InstanceID.process]" is used instead.
     * @return [Pair] with 2 [Set]s - Old State IDs Removed, and New State IDs Added
     */
    @Suppress("MemberVisibilityCanBePrivate")
    override fun refreshInstanceTitle(): Pair<Set<InstanceStateIdDescription>, Set<InstanceStateIdDescription>> {
        refreshInstanceTitleCalc()

        val tpIdPrefix = tpIdPrefixTitle
        val tpLabelPrefix = "Instance $instanceTitleCleaned (#$instanceNumber)"

        // List to put new IDs
        val listNewIDs: MutableSet<InstanceStateIdDescription> = mutableSetOf()

        stateIDTitledInstanceTitle = InstanceStateIdDescription("$tpIdPrefix.title", "$tpLabelPrefix: Title")
        listNewIDs.add(stateIDTitledInstanceTitle)

        nodesList.entries.forEach { nodeType ->
            val nodeTypeName = nodeType.key
            nodeTypeName.perNodeTypeAction(
                stateEventsAction = { nodesStateEventsMap.values },
                booleanAction = { nodesBooleanMap.values },
                numberAction = { nodesNumberMap.values },
                otherAction = { LOGGER.debug { "Unknown Node Type: $nodeTypeName" }; null /*throw IllegalStateException("Unknown Node Type: $nodeTypeName") */ }
            )?.forEach { node ->
                generateNodeDataStateIds(tpIdPrefix, tpLabelPrefix, node) { listNewIDs.add(it); }
            }
        }

        val currentIds = _currentInstanceTitleStateIDs
        val addedIds = listNewIDs.subtract(currentIds)
        val removedIds = currentIds.subtract(listNewIDs)

        _currentInstanceTitleStateIDs = listNewIDs

        return Pair(removedIds, addedIds)
    }

    /**
     * Clears [currentInstanceTitleStateIDs] and returns removed IDs for processing
     * @see refreshInstanceTitle
     * @return [Set] - Old State IDs
     */
    @Suppress("MemberVisibilityCanBePrivate")
    override fun clearInstanceTitle(): Set<InstanceStateIdDescription> {
        val currentIds = _currentInstanceTitleStateIDs
        _currentInstanceTitleStateIDs = mutableSetOf()
        return currentIds
    }

    override fun getInstanceNodePropertyNumberValues(map: MutableMap<String, String>): Map<String, String> =
        getInstanceNodePropertyValues(tpIdPrefixNumber, map)

    override fun getInstanceNodePropertyTitleValues(map: MutableMap<String, String>): Map<String, String> =
        getInstanceNodePropertyValues(tpIdPrefixTitle, map)

    fun getInstanceNodePropertyValues(tpIdPrefix: String, map: MutableMap<String, String>): Map<String, String> {
        map["$tpIdPrefix.title"] = instanceTitleCleaned
        nodesList.entries.forEach { nodeType ->
            val nodeTypeName = nodeType.key
            nodeTypeName.perNodeTypeAction(
                stateEventsAction = { nodesStateEventsMap.values },
                booleanAction = { nodesBooleanMap.values },
                numberAction = { nodesNumberMap.values },
                otherAction = { LOGGER.debug { "Unknown Node Type: $nodeTypeName" }; null /*throw IllegalStateException("Unknown Node Type: $nodeTypeName") */ }
            )?.forEach { node ->
                generateNodePropertyIdValue(nodePrefix = tpIdPrefix, node = node)
                { id, value -> map[id] = value }
            }
        }
        return map
    }

    override fun getInstanceNodePropertyValues(map: MutableMap<String, String>): Map<String, String> {
        nodesList.entries.forEach { nodeType ->
            val nodeTypeName = nodeType.key
            nodeTypeName.perNodeTypeAction(
                stateEventsAction = { nodesStateEventsMap.values },
                booleanAction = { nodesBooleanMap.values },
                numberAction = { nodesNumberMap.values },
                otherAction = { LOGGER.debug { "Unknown Node Type: $nodeTypeName" }; null /*throw IllegalStateException("Unknown Node Type: $nodeTypeName") */ }
            )?.forEach { node ->
                generateNodePropertyIdValueMulti(node = node) { id, value -> map[id] = value }
            }
        }

        return map
    }

    /**
     * Fully refreshes [currentInstanceNumberedStateIDs] with current Node State IDs
     * @return [Pair] with 2 [Set]s - Old State IDs Removed, and New State IDs Added
     */
    override fun refreshInstanceNumber(): Pair<Set<InstanceStateIdDescription>, Set<InstanceStateIdDescription>> {
        val tpIdPrefix = tpIdPrefixNumber
        val tpLabelPrefix = "Instance #$instanceNumber"

        // List old IDs and Generate new ones
        val listNewIDs: MutableSet<InstanceStateIdDescription> = mutableSetOf()

        stateIDNumberedInstanceTitle = InstanceStateIdDescription("$tpIdPrefix.title", "$tpLabelPrefix: Title")
        listNewIDs.add(stateIDNumberedInstanceTitle)

        nodesList.entries.forEach { nodeType ->
            val nodeTypeName = nodeType.key
            nodeTypeName.perNodeTypeAction(
                stateEventsAction = { nodesStateEventsMap.values },
                booleanAction = { nodesBooleanMap.values },
                numberAction = { nodesNumberMap.values },
                otherAction = { LOGGER.debug { "Unknown Node Type: $nodeTypeName" }; null /*throw IllegalStateException("Unknown Node Type: $nodeTypeName")*/ }
            )?.forEach { node ->
                generateNodeDataStateIds(tpIdPrefix, tpLabelPrefix, node) { listNewIDs.add(it); }
            }
        }

        val currentIds = _currentInstanceNumberedStateIDs
        val addedIds = listNewIDs.subtract(currentIds)
        val removedIds = currentIds.subtract(listNewIDs)

        // Update Internal List, Return Old List
        _currentInstanceNumberedStateIDs = listNewIDs

        return Pair(removedIds, addedIds)
    }

    /**
     * Clears [currentInstanceNumberedStateIDs] and returns the Node State IDs
     * @see refreshInstanceNumber
     * @return [Set] - State IDs Removed
     */
    override fun clearInstanceNumber(): Set<InstanceStateIdDescription> {
        val currentIds = _currentInstanceNumberedStateIDs
        _currentInstanceNumberedStateIDs = mutableSetOf()
        return currentIds
    }

    /**
     * Generated State ID for mini Window Title
     * - e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.FullInstances.state.1.currentAvatarStateName
     * - Basically returns [baseStateID].[stateIDNumberedInstanceTitle]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val stateIDNumberedInstanceTitleLong; get() = "${baseStateID}.$stateIDNumberedInstanceTitle"

    inline val tpStateIDInstanceNodePrefixNumber; get() = "$instanceNumber.nodes."

    inline val tpStateIDInstanceNodePrefixTitle; get() = "$instanceTitleSimplified.nodes."

    /**
     * Update Node from Message
     *
     * Runs the following for each [message] Type:
     * - [ResultMessage.ResultMessageWithPayload] - [updateNodeState]
     * - [ResultMessage.ResultMessageWithPayloadBoolean] - [updateNodeCurrentBooleanValue]
     * - [ResultMessage.ResultMessageWithPayloadNumber] - [updateNodeNumberValue]
     * - [ResultMessage.ResultMessageWithInstanceInfo] - Ignored
     * - [ResultMessage.ResultMessageWithNodeEntryList] - **Throws [IllegalArgumentException]**
     * @param message Result Message. Must **not** be [ResultMessage.ResultMessageWithNodeEntryList].
     * @return Whether state was updated - false means the state is already the one provided
     */
    @Throws(IllegalArgumentException::class)
    fun updateStateNodeCurrentState(message: ResultMessage): INodeUpdateResult? = when (message) {
        is BleatkanPayload -> updateNodeState(message)
        is BleatkanPayloadBoolean -> updateNodeCurrentBooleanValue(message)
        is BleatkanPayloadNumber -> updateNodeNumberValue(message)
        is ResultMessage.ResultMessageWithInstanceInfo -> {
            LOGGER.trace { "Ignoring message of type ResultMessageWithInstanceInfo: $message" }
            null
        }
        is ResultMessage.ResultMessageWithNodeEntryList ->
            throw IllegalArgumentException("Message type can not be ResultMessageWithNodeEntryList")
    }

    /**
     * Update Node Value
     *
     * @param message Result Message
     *
     * @return Whether state was updated - false means the state is already the one provided
     */
    fun updateNodeState(message: BleatkanPayload): UpdateNodeStateResult {
        return when (val payload = message.payload) {
            is BleatkanStatePeek ->
                updateStateNodeCurrentState(message.id, message.name, payload)
            is BleatkanStateThumbnail ->
                updateStateNodeThumbnail(message.id, message.name, payload)
            is BleatkanStateList ->
                updateNodeStates(message.id, message.name, payload)
        }
    }

    /**
     * Update Current State
     *
     * @param nodeId ID of Node
     * @param nodeId Name of Node - Used if Node doesn't exist
     * @param payloadState ID from Peek/Listen Result Message
     *
     * @return Whether state was updated - false means the state is already the one provided
     */
    fun updateStateNodeCurrentState(
        nodeId: String, nodeName: String = "", payloadState: BleatkanStatePeek
    ): UpdateNodeStateResult {
        var isNodeCreated = false

        val node = getOrCreateStateEventsNode(nodeId, nodeName) { isNodeCreated = true }

        val nameUpdated = if (!isNodeCreated && nodeName != node.name) { // Name Update
            node.updateNodeName(nodeName)
        } else null

        val isNodeUpdated = node.updateCurrentState(payloadState)

        return UpdateNodeStateResult(
            nodeCreated = isNodeCreated, nodeValueUpdated = isNodeUpdated,
            nodeOldName = nameUpdated, nodeData = node
        )
    }

    /**
     * Replaces all states
     *
     * If the list's don't match, items are checked and updated
     *
     * @param nodeId ID of Node
     * @param nodeId Name of Node
     * @param payloadStateList List of States from Result Message
     *
     * @return Whether state was updated - false means the state is already the one provided
     */
    fun updateNodeStates(
        nodeId: String, nodeName: String = "", payloadStateList: BleatkanStateList
    ): UpdateNodeStateResult {
        var isNodeCreated = false
        val node = getOrCreateStateEventsNode(nodeId, nodeName) { isNodeCreated = true }

        val nameUpdated = if (!isNodeCreated && nodeName != node.name) { // Name Update
            node.updateNodeName(nodeName)
        } else null

        val isNodeUpdated = node.updateStates(payloadStateList)

        return UpdateNodeStateResult(
            nodeCreated = isNodeCreated, nodeValueUpdated = isNodeUpdated,
            nodeOldName = nameUpdated, nodeData = node
        )
    }

    /**
     * Updates a State Thumbnail, creating the state if it doesn't exist
     *
     * @param payload Payload with State Thumbnail Data to update
     * @param updateToMRU forces the State Thumbnail to be made the *Most Recently Used* on in the Soft and Hard Thumbnail Maps
     *
     * @return Whether state was updated - false means the state is already the one provided
     */
    fun updateStateNodeThumbnail(
        nodeId: String, nodeName: String = "",
        payload: BleatkanStateThumbnail, updateToMRU: Boolean = false
    ): UpdateNodeStateResult {
        var isNodeCreated = false

        val node = getOrCreateStateEventsNode(nodeId, nodeName) { isNodeCreated = true }

        val nameUpdated = if (!isNodeCreated && nodeName != node.name) { // Name Update
            node.updateNodeName(nodeName)
        } else null

        val (isNodeUpdated, second, third) = node.updateStateThumbnail(payload, updateToMRU)

        return UpdateNodeStateResult(
            nodeCreated = isNodeCreated, nodeValueUpdated = isNodeUpdated,
            nodeOldName = nameUpdated, nodeData = node
        )
    }

    /**
     * Updates a State Thumbnail, creating the state if it doesn't exist
     *
     * @param payload Payload with State Thumbnail Data to update
     * @param updateToMRU forces the State Thumbnail to be made the *Most Recently Used* on in the Soft and Hard Thumbnail Maps
     *
     * @return Pair: First = Boolean, if thumbnail was Updated; Second = VeadoState Updated, Third = VeadoThumbnail?, thumbnail object if updated
     */
    fun updateStateNodeThumbnailReturnResult(
        nodeId: String, nodeName: String = "",
        payload: BleatkanStateThumbnail, updateToMRU: Boolean = false
    ): Triple<Boolean, VeadoStateNodeData, VeadoState> {
        val node = getOrCreateStateEventsNode(nodeId, nodeName)

        val (thumbnailUpdated, updateState, newThumbnail) = node.updateStateThumbnail(payload, updateToMRU)

        return Triple(thumbnailUpdated, node, updateState)
    }

    /**
     * Clears a State Thumbnail
     *
     * @return Whether thumbnail was cleared - false means the there was no thumbnail stored
     */
    fun clearStateNodeThumbnail(
        nodeId: String, veadoState: VeadoState
    ): Boolean {
        val node = nodesStateEventsMap[nodeId] ?: return false;
        return node.clearedStateThumbnail(veadoState)
    }

    /**
     * Update Value of Boolean Node
     *
     * @param message Get/Listen Result Message
     * @return Whether state was updated - false means the state is already the one provided
     */
    fun updateNodeCurrentBooleanValue(message: BleatkanPayloadBoolean): UpdateNodeBooleanResult {
        val typeNodes = nodesBooleanMap
        var nodeValueCreated = false
        val node = typeNodes.getOrPut(message.id) {
            nodeValueCreated = true
            VeadoBooleanNodeData(message.id, message.name, "boolean")
        }

        val nameUpdated =
            if (!nodeValueCreated && message.name != node.name) { // Name Update
                node.updateNodeName(message.name)
            } else null

        val nodeValueUpdated = (node.updateNodeValue(message.payload) != message.payload)
        return UpdateNodeBooleanResult(
            nodeCreated = nodeValueCreated, nodeValueUpdated = nodeValueUpdated,
            nodeOldName = nameUpdated, nodeData = node
        )
    }

    /**
     * Update Value of Number Node
     *
     * @param message Get/Listen Result Message
     *
     * @return Whether state was updated - false means the state is already the one provided
     */
    fun updateNodeNumberValue(message: BleatkanPayloadNumber): UpdateNodeNumberResult {
        val nodeId: String = message.id
        val nodeName: String = message.name
        val payloadValue: ResultPayloadSpecialNumber = message.payload


        val typeNodes = nodesNumberMap

        var nodeValueCreated = false
        val node = typeNodes.getOrPut(nodeId) {
            nodeValueCreated = true
            VeadoNumberNodeData(nodeId, nodeName, "number")
        }

        val nameUpdated =
            if (!nodeValueCreated && message.name != node.name) { // Name Update
                node.updateNodeName(message.name)
            } else null

        var nodeValueUpdated = false

        val payloadNumber = payloadValue.value
        val latestNumber = node.updateNodeValue(payloadNumber)
        if (latestNumber != payloadNumber) nodeValueUpdated = true

        val payloadMin = payloadValue.min
        val latestMin = node.updateNodeMin(payloadMin)
        if (latestMin != payloadMin) nodeValueUpdated = true

        val payloadMax = payloadValue.max
        val latestMax = node.updateNodeMax(payloadMax)
        if (latestMax != payloadMax) nodeValueUpdated = true

        return UpdateNodeNumberResult(
            nodeCreated = nodeValueCreated, nodeValueUpdated = nodeValueUpdated,
            nodeOldName = nameUpdated, nodeData = node
        )
    }

    /** Update Nodes
     *
     * @param entries List of Entries from [io.github.dissonantau.bleatkan.message.ResultMessage.ResultMessageWithNodeEntryList.entries]
     * @return Pair with 2 Lists of Strings: Added Nodes & Removed Nodes.
     *
     * @throws IllegalArgumentException if Type Not Recognised
     */
    @Throws(IllegalArgumentException::class)
    fun updateNodes(entries: List<Entry>): Triple<List<VeadoNodeData>, List<VeadoNodeData>, List<VeadoNodeData>> {
        val addedList: MutableList<VeadoNodeData> = mutableListOf()
        val removedList: MutableList<VeadoNodeData> = mutableListOf()
        val modifiedList: MutableList<VeadoNodeData> = mutableListOf()

        // Add all to Nodes to Removed List - we then remove them as they're in the entries list
        nodesList.values.forEach { it.values.forEach { nodeData -> removedList.add(nodeData) } }

        entries.forEach { entry ->
            var isNodeNew = false

            entry.type.perNodeTypeAction(
                stateEventsAction = {
                    val node = getOrCreateStateEventsNode(entry.id, entry.name) { isNodeNew = true }
                    if (isNodeNew) {
                        addedList.add(node)
                    } else {
                        // Update Node Name if different
                        if (node.name != entry.name) {
                            node.updateNodeName(entry.name)
                            modifiedList.add(node)
                        }
                        removedList.remove(node)
                    }
                },
                booleanAction = {
                    val node = getOrCreateBooleanNode(entry.id, entry.name) { isNodeNew = true }
                    if (isNodeNew) {
                        addedList.add(node)
                    } else {
                        // Update Node Name if different
                        if (node.name != entry.name) {
                            node.updateNodeName(entry.name)
                            modifiedList.add(node)
                        }
                        removedList.remove(node)
                    }
                },
                numberAction = {
                    val node = getOrCreateNumberNode(entry.id, entry.name) { isNodeNew = true }
                    if (isNodeNew) {
                        addedList.add(node)
                    } else {
                        // Update Node Name if different
                        if (node.name != entry.name) {
                            node.updateNodeName(entry.name)
                            modifiedList.add(node)
                        }
                        removedList.remove(node)
                    }
                }
            )

        }

        //Remove nodes still in removedList
        removedList.forEach {
            nodesList[it.type]?.remove(it.id)
        }

        return Triple(addedList, removedList, modifiedList)
    }

    private inline fun getOrCreateStateEventsNode(
        nodeId: String, nodeName: String = "", onCreateNodeActions: () -> Unit = {}
    ): VeadoStateNodeData =
        nodesStateEventsMap.getOrPut(nodeId) {
            onCreateNodeActions()
            VeadoStateNodeData(nodeId, nodeName, "stateEvents")
        }

    private inline fun getOrCreateBooleanNode(
        nodeId: String, nodeName: String = "", onCreateNodeActions: () -> Unit = {}
    ): VeadoBooleanNodeData =
        nodesBooleanMap.getOrPut(nodeId) {
            onCreateNodeActions()
            VeadoBooleanNodeData(nodeId, nodeName, "boolean")
        }

    private inline fun getOrCreateNumberNode(
        nodeId: String, nodeName: String = "", onCreateNodeActions: () -> Unit = {}
    ): VeadoNumberNodeData =
        nodesNumberMap.getOrPut(nodeId) {
            onCreateNodeActions()
            VeadoNumberNodeData(nodeId, nodeName, "number")
        }

    private inline fun removeStateEventsNode(nodeId: String): VeadoStateNodeData? = nodesStateEventsMap.remove(nodeId)

    private inline fun removeBooleanNode(nodeId: String): VeadoBooleanNodeData? = nodesBooleanMap.remove(nodeId)

    private inline fun removeNumberNode(nodeId: String): VeadoNumberNodeData? = nodesNumberMap.remove(nodeId)

    /**
     * Indicates if the node list is stale
     *
     * This is marked true when a node is added outside a complete list refresh, and it should be fetched, etc.
     */
    private var nodeListStale = true

    /**
     *
     * @return `true` if node was updated
     */
    fun updateStateNodeList(
        message: BleatkanPayload, payload: BleatkanStateList
    ): Boolean {
        val nodeId = message.id
        val nodeName = message.name

        var isNodeNew = false

        val node = getOrCreateStateEventsNode(nodeId, nodeName) { isNodeNew = true }
        if (isNodeNew) {
            nodeListStale = true // TODO may not be needed
        } else { // Update Node Name if different
            if (node.name != nodeName) node.updateNodeName(nodeName)
        }

        val nodeUpdated = node.updateStates(payload)

        return (nodeUpdated || isNodeNew)
    }

    /**
     *
     * @return [Pair] First [Boolean] is `true` if node was updated; Second [VeadoStateNodeData] is the node that was updated
     */
    fun updateReturnStateNodeList(
        message: BleatkanPayload, payload: BleatkanStateList
    ): Pair<Boolean, VeadoStateNodeData> {
        val nodeId = message.id
        val nodeName = message.name

        var isNodeNew = false

        val node = getOrCreateStateEventsNode(nodeId, nodeName) { isNodeNew = true }
        if (isNodeNew) {
            nodeListStale = true // TODO may not be needed
        } else { // Update Node Name if different
            if (node.name != nodeName) node.updateNodeName(nodeName)
        }

        val nodeUpdated = node.updateStates(payload)

        return Pair((nodeUpdated || isNodeNew), node)
    }

}
