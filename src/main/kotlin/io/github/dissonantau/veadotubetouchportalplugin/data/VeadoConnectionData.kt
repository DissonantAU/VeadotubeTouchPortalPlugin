package io.github.dissonantau.veadotubetouchportalplugin.data

import io.github.dissonantau.bleatkan.connection.Connection
import io.github.dissonantau.bleatkan.instance.Instance
import io.github.dissonantau.bleatkan.instance.InstanceID
import io.github.oshai.kotlinlogging.KotlinLogging
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.lang.ref.WeakReference
import kotlin.jvm.Throws

abstract class VeadoConnectionData(connection: Connection) {

    companion object {
        internal val LOGGER = KotlinLogging.logger {}

        /**
         * Compares Pair with String Match Value (Descending) then by Title Length
         *
         * Pair First should be Int representing match value, higher is closer match
         * Pair Second should be VeadoNodeData
         */
        val COMPARE_INSTANCE_BY_NAME =
            compareByDescending<Pair<Int, VeadoNodeData>> { it.first }.thenBy { it.second.name.length }

    }

    /** Connection Weak Ref - used to prevent GC issues */
    private var _connection: WeakReference<Connection> = WeakReference(connection)

    /** [Connection] this data belongs to.
     *
     *  Backed by a [WeakReference] - returns null if Connection has been dereferenced elsewhere and cleaned
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val connection: Connection?
        get() = _connection.get()


    /** Instance this data belongs to */
    val instance: Instance = connection.instance

    /** Instance Type
     *
     * 'mini' 'veado' (full), etc.
     *
     * See [InstanceID.type]
     */
    val type: String = instance.id.type


    /** Updates Connection linked to Object
     *
     * Should be used when a connection is lost and reestablished and cached data should be kept
     *
     * A [WeakReference] is used to prevent GC issues
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun updateConnection(connection: Connection) {
        _connection = WeakReference(connection)
    }

    /**
     * Stable Instance Number for Mapping in Touch Portal
     *
     * Related to Connection order (1st 2nd, etc.)
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var instanceNumber = -1
        internal set

    /** Set Stable Instance Number
     *
     * This number should not change while the Instance is active, even when the Connection is lost or restarted
     *
     * @param newInstanceNumber Instance Number to be used. Must be 1 or greater
     * @return Previous Instance Number. -1 if Instance Number was not previously set
     * @throws IllegalArgumentException If [instanceNumber] has not been set
     */
    @Suppress("MemberVisibilityCanBePrivate")
    @Throws(IllegalArgumentException::class)
    fun setInstanceNumber(newInstanceNumber: Int): Int {
        require(newInstanceNumber > 0) { "Instance Number must be greater than 0" }
        val oldInstanceNumber = instanceNumber
        instanceNumber = newInstanceNumber
        return oldInstanceNumber
    }

    /** Instance Title
     *
     * Simplified Title after first Dash, if one is set
     * * only Letters and Digits are kept - no spaces, etc. ( e.g. 'veadotube mini - my @ title 3!' becomes 'mytitle3')
     *
     * If no non-default title exists (e.g. 'veadotube mini') the Type & Process ID are used (mini123456)
     *
     * This should be updated if title changes using [refreshInstanceTitle]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var instanceTitleSimplified: String = ""
        internal set

    /** Instance Title
     *
     * Cleaned Title after first Dash, if one is set
     * * Trimmed ( e.g. 'veadotube mini - my @ title 3!' becomes 'my @ title 3!')
     *
     * If no non-default title exists (e.g. 'veadotube mini') the Type & Process ID are used (mini-123456)
     *
     * This should be updated if title changes using [refreshInstanceTitle]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var instanceTitleCleaned: String = ""
        internal set

    /**
     * Base State ID used for Dynamically creating and deleting States
     *
     * Excludes trailing dot (.)
     *
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state
     */
    abstract val baseStateID: String

    /**
     * Updates State IDs that use the Instance Number
     *
     * @return List of the previous State IDs for Removal
     * @throws IllegalStateException If [instanceNumber] has not been set
     */
    @Throws(IllegalStateException::class)
    abstract fun refreshInstanceNumber(): Pair<Set<InstanceStateIdDescription>, Set<InstanceStateIdDescription>>

    /**
     * Updates State IDs that use [Instance.title][io.github.dissonantau.bleatkan.instance.Instance.title]
     *
     * If nothing exists after the first Dash in the Title, "[InstanceID.type][io.github.dissonantau.bleatkan.instance.InstanceID.type]-[InstanceID.process][io.github.dissonantau.bleatkan.instance.InstanceID.process]" is used
     *
     * @return [Pair] with 2 [Set]s - Old State IDs Removed, and New State IDs Added
     */
    abstract fun refreshInstanceTitle(): Pair<Set<InstanceStateIdDescription>, Set<InstanceStateIdDescription>>

    /**
     * Clears [instanceNumber] based Title IDs and returns removed IDs for processing
     *
     * @see refreshInstanceNumber
     * @return [Set] - Old State IDs
     */
    @Throws(IllegalStateException::class)
    abstract fun clearInstanceNumber(): Set<InstanceStateIdDescription>

    /**
     * Clears [instanceTitleSimplified] based Title IDs and returns removed IDs for processing
     *
     * @see refreshInstanceTitle
     * @return [Set] - Old State IDs
     */
    abstract fun clearInstanceTitle(): Set<InstanceStateIdDescription>


    /**
     * UPDATE DESC
     * Updates State IDs that use the Instance Number
     *
     * @return List of the previous State IDs for Removal
     * @throws IllegalStateException If [instanceNumber] has not been set
     */
    @Throws(IllegalStateException::class)
    abstract fun getInstanceNodePropertyNumberValues(map: MutableMap<String, String> = mutableMapOf()): Map<String, String>

    /**
     * UPDATE DESC
     * Updates State IDs that use [Instance.title][io.github.dissonantau.bleatkan.instance.Instance.title]
     *
     * If nothing exists after the first Dash in the Title, "[InstanceID.type][io.github.dissonantau.bleatkan.instance.InstanceID.type]-[InstanceID.process][io.github.dissonantau.bleatkan.instance.InstanceID.process]" is used
     *
     * @return [Pair] with 2 [Set]s - Old State IDs Removed, and New State IDs Added
     */
    abstract fun getInstanceNodePropertyTitleValues(map: MutableMap<String, String> = mutableMapOf()): Map<String, String>

    /**
     * UPDATE DESC
     * Updates State IDs that use [Instance.title][io.github.dissonantau.bleatkan.instance.Instance.title]
     *
     * If nothing exists after the first Dash in the Title, "[InstanceID.type][io.github.dissonantau.bleatkan.instance.InstanceID.type]-[InstanceID.process][io.github.dissonantau.bleatkan.instance.InstanceID.process]" is used
     *
     * @return [Map] with TP IDs vs value
     */
    abstract fun getInstanceNodePropertyValues(map: MutableMap<String, String> = mutableMapOf()): Map<String, String>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VeadoConnectionData

        if (type != other.type) return false
        if (instance.id != other.instance.id) return false
        if (instance != other.instance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + instance.id.hashCode()
        result = 31 * result + instance.hashCode()
        return result
    }

    /**
     * Get Titled TP State ID for a Node. Excludes last dot.
     *
     * `state.<InstanceTitle>.nodes.<type>.<nodeId>`
     */
    fun getStateIdTitledForNode(node: VeadoNodeData, nodeId: String = node.id) =
        buildString {
            append(instanceTitleSimplified).append(".nodes.").append(node.type).append(".").append(nodeId)
        }

    /**
     * Get Numbered TP State ID for a Node. Excludes last dot.
     *
     * `state.<InstanceNumber>.nodes.<type>.<nodeId>`
     */
    fun getStateIdNumberedForNode(node: VeadoNodeData, nodeId: String = node.id) =
        buildString {
            append(instanceNumber).append(".nodes.").append(node.type).append(".").append(nodeId)
        }

    /**
     * Get Titled and Numbered TP State ID for a Node. Excludes last dot.
     *
     * @return Pair(`<InstanceTitle>.nodes.<type>.<nodeId>` , `<InstanceNumber>.nodes.<type>.<nodeId>`)
     */
    fun getStateIdTitledNumberedForNode(node: VeadoNodeData, nodeId: String = node.id): Pair<String, String> {
        val shared = buildString {
            append("nodes.").append(node.type).append(".").append(nodeId)
        }

        return Pair("$instanceTitleSimplified.$shared", "$instanceNumber.$shared")
    }

    /**
     * Generates the ID and Label for all of a connection's node properties and passes to action lambdas
     *
     * @param node Node
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     */
    inline fun getTPStateIdLabelForNode(
        node: VeadoNodeData,
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit
    ) {
        val shared = ".nodes.${node.type}.${node.id}."

        // <InstanceNodePrefix>.nodes.<type>.<nodeId>
        val stateIdNodesIdT = "$instanceTitleSimplified${shared}"
        val stateIdNodesIdN = "$instanceNumber${shared}"

        val tpLabelPrefixT = "Instance $instanceTitleCleaned (#$instanceNumber): Node ${node.name} - "
        val tpLabelPrefixN = "Instance #$instanceNumber: Node ${node.name} - "

        // Get List of IDs, append to stateIdTitledNodesId and add to new ID list
        node.getNodeTPStateIDLabelMap().forEach { (stateId, stateLabel) ->
            /* Title - <InstanceTitle>.nodes.<type>.<nodeId>.<stateId> */
            instanceTPStateIdNumber(
                "$stateIdNodesIdT$stateId", "$tpLabelPrefixT$stateLabel"
            )
            /* Number - <InstanceNumber>.nodes.<type>.<nodeId>.<stateId> */
            instanceTPStateIdTitle(
                "$stateIdNodesIdN$stateId", "$tpLabelPrefixN$stateLabel"
            )
        }
    }

    fun tpTitlePropertyLabel(node: VeadoNodeData): String {
        return "Instance $instanceTitleCleaned (#$instanceNumber): Node ${node.name} - "
    }

    fun tpNumberPropertyLabel(node: VeadoNodeData): String {
        return "Instance #$instanceNumber: Node ${node.name} - "
    }

    /**
     * Generates the ID and Label for a connection's node property and passes to action lambdas
     *
     * @param node Node
     * @param propertyId ID of Property - e.g. id, name
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     * @param actionValueIdNotFound Action executed if Value ID isn't found in Label Map
     */
    inline fun getTPStateIdLabelNodeProperty(
        node: VeadoNodeData, propertyId: String,
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        actionValueIdNotFound: () -> Unit = {}
    ) {
        val shared = ".nodes.${node.type}.${node.id}."

        // <InstanceNodePrefix>.nodes.<type>.<nodeId>
        val stateIdNodesIdT = "$instanceTitleSimplified${shared}"
        val stateIdNodesIdN = "$instanceNumber${shared}"

        val tpLabelPrefixT = "Instance $instanceTitleCleaned (#$instanceNumber): Node ${node.name} - "
        val tpLabelPrefixN = "Instance #$instanceNumber: Node ${node.name} - "

        // Get List of IDs, append to stateIdTitledNodesId and add to new ID list
        node.getNodeTPStateIDLabelMap()[propertyId]?.also { stateLabel ->
            /* Title - <InstanceTitle>.nodes.<type>.<nodeId>.<stateId> */
            instanceTPStateIdNumber(
                "$stateIdNodesIdT$propertyId", "$tpLabelPrefixT$stateLabel"
            )
            /* Number - <InstanceNumber>.nodes.<type>.<nodeId>.<stateId> */
            instanceTPStateIdTitle(
                "$stateIdNodesIdN$propertyId", "$tpLabelPrefixN$stateLabel"
            )
        } ?: actionValueIdNotFound()
    }

    inline fun generateNodeDataStateIdsMulti(
        nodePrefixes: Array<String> = arrayOf(instanceTitleSimplified, instanceNumber.toString()),
        nodeLabelPrefix: String, node: VeadoNodeData,
        postProcessActions: (InstanceStateIdDescription) -> Unit = {}
    ) {
        // <nodePrefix>.nodes.<type>.<nodeId>
        val stateIdNodesId = ".nodes.${node.type}.${node.id}."
        // Get List of IDs, append to stateIdTitledNodesId and add to new ID list
        node.getNodeDataStateIds().forEach { stateIdInfo ->
            nodePrefixes.forEach { nodePrefix ->
                postProcessActions(
                    InstanceStateIdDescription(
                        // <InstanceTitle>.nodes.<type>.<nodeId>.<stateId>
                        "${nodePrefix}$stateIdNodesId${stateIdInfo.stateId}",
                        "$nodeLabelPrefix: Node ${node.name} - ${stateIdInfo.stateLabel}"
                    )
                )
            }
        }
    }

    inline fun generateNodeDataStateIds(
        nodePrefix: String, nodeLabelPrefix: String,
        node: VeadoNodeData, postProcessActions: (InstanceStateIdDescription) -> Unit = {}
    ) {
        // <nodePrefix>.nodes.<type>.<nodeId>
        val stateIdNodesId = "${nodePrefix}.nodes.${node.type}.${node.id}."
        // Get List of IDs, append to stateIdTitledNodesId and add to new ID list
        node.getNodeDataStateIds().forEach { stateIdInfo ->
            postProcessActions(
                InstanceStateIdDescription(
                    // <InstanceTitle>.nodes.<type>.<nodeId>.<stateId>
                    "$stateIdNodesId${stateIdInfo.stateId}",
                    "$nodeLabelPrefix: Node ${node.name} - ${stateIdInfo.stateLabel}"
                )
            )
        }
    }

    inline fun generateNodePropertyIdLabelMulti(
        nodePrefixes: Array<String> = arrayOf(instanceTitleSimplified, instanceNumber.toString()),
        nodeLabelPrefix: String, node: VeadoNodeData,
        postProcessActions: (InstanceStateIdDescription) -> Unit = {}
    ) {
        // <nodePrefix>.nodes.<type>.<nodeId>
        val stateIdNodesId = ".nodes.${node.type}.${node.id}."
        // Get List of IDs, append to stateIdTitledNodesId and add to new ID list
        node.getNodePropertyIdLabels().forEach { (stateId, stateLabel) ->
            nodePrefixes.forEach { nodePrefix ->
                postProcessActions(
                    InstanceStateIdDescription(
                        // <InstanceTitle>.nodes.<type>.<nodeId>.<stateId>
                        "$nodePrefix$stateIdNodesId$stateId",
                        "$nodeLabelPrefix: Node ${node.name} - $stateLabel"
                    )
                )
            }
        }
    }

    inline fun generateNodePropertyIdLabel(
        nodePrefix: String,
        nodeLabelPrefix: String,
        node: VeadoNodeData, postProcessActions: (InstanceStateIdDescription) -> Unit = {}
    ) {
        // <nodePrefix>.nodes.<type>.<nodeId>
        val stateIdNodesId = "${nodePrefix}.nodes.${node.type}.${node.id}."
        // Get List of IDs, append to stateIdTitledNodesId and add to new ID list
        node.getNodePropertyIdLabels().forEach { (stateId, stateLabel) ->
            postProcessActions(
                InstanceStateIdDescription(
                    // <InstanceTitle>.nodes.<type>.<nodeId>.<stateId>
                    "$stateIdNodesId$stateId",
                    "$nodeLabelPrefix: Node ${node.name} - $stateLabel"
                )
            )
        }
    }

    inline fun generateNodePropertyIdValueMulti(
        nodePrefixes: Array<String> = arrayOf(instanceTitleSimplified, instanceNumber.toString()),
        node: VeadoNodeData, postProcessActions: (id: String, value: String) -> Unit = { _, _ -> }
    ) {
        // <nodePrefix>.nodes.<type>.<nodeId>
        val stateIdNodesId = ".nodes.${node.type}.${node.id}."
        // Get List of IDs, append to stateIdTitledNodesId and add to new ID list
        node.getNodePropertyIdValues().forEach { (stateId, stateValue) ->
            nodePrefixes.forEach { nodePrefix ->
                postProcessActions(
                    // <InstanceTitle>.nodes.<type>.<nodeId>.<stateId>
                    "$nodePrefix$stateIdNodesId$stateId", stateValue
                )
            }
        }
    }


    inline fun generateNodePropertyIdValue(
        nodePrefix: String,
        node: VeadoNodeData, postProcessActions: (id: String, value: String) -> Unit = { _, _ -> }
    ) {
        // <nodePrefix>.nodes.<type>.<nodeId>
        val stateIdNodesId = "${nodePrefix}.nodes.${node.type}.${node.id}."
        // Get List of IDs, append to stateIdTitledNodesId and add to new ID list
        node.getNodePropertyIdValues().forEach { (stateId, stateValue) ->
            postProcessActions(
                // <InstanceTitle>.nodes.<type>.<nodeId>.<stateId>
                "$stateIdNodesId$stateId", stateValue
            )
        }
    }

    /**
     * Does a Fuzzy Search on the names of a Map of Nodes
     */
    inline fun nodeNameFuzzySearch(
        nodeMap: Map<String, VeadoNodeData>,
        searchString: String, minimumResultRation: Int = 80,
        resultAction: (resultRatio: Int, node: VeadoNodeData) -> Unit
    ) {
        nodeMap.forEach { nodeEntry ->
            val node = nodeEntry.value
            val resultRatio = FuzzySearch.ratio(searchString, node.name)
            if (resultRatio >= minimumResultRation) resultAction(resultRatio, node)
        }
    }


    /** Inline function for actions based on instance type
     *
     * @param nodeType e.g. 'stateEvents'/'boolean'/'number'
     * @param stateEventsAction Action when Type = 'stateEvents'
     * @param booleanAction Action when Type = 'boolean'
     * @param numberAction Action when Type = 'number'
     * @param otherAction Action when Type is not above. If not set, throws an IllegalArgumentException
     *
     * @throws IllegalArgumentException if Type Not Recognised and [otherAction] is not set
     */
    @Throws(IllegalArgumentException::class)
    internal inline fun <T> String.perNodeTypeAction(
        stateEventsAction: (nodeType: String) -> T,
        booleanAction: (nodeType: String) -> T,
        numberAction: (nodeType: String) -> T,
        otherAction: (nodeType: String) -> T = { throw IllegalArgumentException("Unknown Node Type: $it") }
    ): T {
        return when (this) {
            "stateEvents" -> stateEventsAction(this)
            "boolean" -> booleanAction(this)
            "number" -> numberAction(this)
            else -> otherAction(this)
        }
    }


    /** Inline function for actions based on Class inheriting [VeadoConnectionData].
     *
     * @param miniConnectionDataAction Action when VeadoMiniConnectionData
     * @param fullConnectionDataAction Action when VeadoFullConnectionData
     * @param otherAction Action when not one of the above Classes, passed as [VeadoConnectionData]
     */
    inline fun VeadoConnectionData.perConnectionDataAction(
        miniConnectionDataAction: (VeadoMiniConnectionData) -> Unit,
        fullConnectionDataAction: (VeadoFullConnectionData) -> Unit,
        otherAction: (VeadoConnectionData) -> Unit = {}
    ) {
        when (this) {
            is VeadoMiniConnectionData -> miniConnectionDataAction(this)
            is VeadoFullConnectionData -> fullConnectionDataAction(this)
            else -> otherAction(this)
        }
    }

}


data class UpdateInstanceResult(
    var connection: Connection?,
    var oldIDs: Set<InstanceStateIdDescription>,
    var newTitleIDs: Set<InstanceStateIdDescription>,
    var newNumberIDs: Set<InstanceStateIdDescription>
)

data class InstanceStateIdDescription(
    val stateId: String,
    val stateLabel: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InstanceStateIdDescription

        if (stateId != other.stateId) return false
        if (stateLabel != other.stateLabel) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stateId.hashCode()
        result = 31 * result + stateLabel.hashCode()
        return result
    }
}


interface INodeUpdateResult {
    val nodeCreated: Boolean
    val nodeValueUpdated: Boolean
    val nodeOldName: String?
}

/** Result of Boolean Node Update
 * @param nodeCreated true if data is for a new node
 * @param nodeValueUpdated true if the node's value was updated
 * @param nodeOldName String containing old name if the Touch Portal node ID should be updated `null` if not
 * @param nodeData the Node Object created/updated
 */
data class UpdateNodeBooleanResult(
    override val nodeCreated: Boolean, override val nodeValueUpdated: Boolean,
    override val nodeOldName: String?, val nodeData: VeadoBooleanNodeData
) : INodeUpdateResult

/** Result of Number Node Update
 * @param nodeCreated true if data is for a new node
 * @param nodeValueUpdated true if the node's value was updated
 * @param nodeOldName String containing old name if the Touch Portal node ID should be updated `null` if not
 * @param nodeData the Node Object created/updated
 */
data class UpdateNodeNumberResult(
    override val nodeCreated: Boolean, override val nodeValueUpdated: Boolean,
    override val nodeOldName: String?, val nodeData: VeadoNumberNodeData
) : INodeUpdateResult

/** Result of State Node Update
 * @param nodeCreated true if data is for a new node
 * @param nodeValueUpdated true if the node's value was updated
 * @param nodeOldName String containing old name if the Touch Portal node ID should be updated `null` if not
 * @param nodeData the Node Object created/updated
 */
data class UpdateNodeStateResult(
    override val nodeCreated: Boolean, override val nodeValueUpdated: Boolean,
    override val nodeOldName: String?, val nodeData: VeadoStateNodeData
) : INodeUpdateResult
