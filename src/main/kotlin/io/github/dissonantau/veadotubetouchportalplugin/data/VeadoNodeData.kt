package io.github.dissonantau.veadotubetouchportalplugin.data

import io.github.oshai.kotlinlogging.KotlinLogging

abstract class VeadoNodeData(
    val id: String,
    name: String,
    val type: String
) {

    companion object {
        @JvmStatic
        internal val LOGGER = KotlinLogging.logger {}

        /**
         * Node Map of Fixed TP Data IDs.
         *
         * These are the fixed/Core IDs that always exist and don't change
         * - no Dynamic State IDs are kept here (e.g. Values of non-active states)
         */
        @JvmStatic
        val DataNodeTPStateIDLabelMap: Map<String, String> = mapOf(
            tpStateNodeIdVeadoId to tpStateNodeDescriptionVeadoId,
            tpStateNodeIdName to tpStateNodeDescriptionName,
            tpStateNodeIdType to tpStateNodeDescriptionType
        )

        /** Touch Portal State ID for Veado Node ID */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdVeadoId; get() = "id"

        /** Touch Portal State ID for Veado Node Name */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdName; get() = "name"

        /** Touch Portal State ID for Veado Node Type */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdType; get() = "type"

        /** Touch Portal State ID for Veado Node ID */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionVeadoId; get() = "ID"

        /** Touch Portal State ID for Veado Node Name */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionName; get() = "Name"

        /** Touch Portal State ID for Veado Node Type */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionType; get() = "Type"
    }

    open var name: String = name
        protected set

    /**
     * Updates Node Name
     *
     * @return Previous Node Name
     */
    fun updateNodeName(newName: String): String {
        val oldName = name
        name = newName
        return oldName
    }

    /**
     * Generated State ID for veadotube Node ID - e.g. `<id>.id`
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inline val stateIDNodeId; get() = "${id}.id"

    /**
     * Generated State ID for veadotube Node Name - e.g. `<id>.name`
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inline val stateIDNodeName; get() = "${id}.name"

    /**
     * Generated State ID for veadotube Node Type - e.g. `<id>.type`
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inline val stateIDNodeType; get() = "${id}.type"

    /** Touch Portal State ID prefix for the Node - e.g. (type).(id) */
    @Suppress("MemberVisibilityCanBePrivate")
    inline val tpStateNodePrefix; get() = "$type.$id"

    /** Is the Node ID the same as the Name */
    inline val idNameSame; get() = id == name

    /**
     * Returns Touch Portal ID and Label of each Property of the node - id, label
     *
     * e.g. `"name"`, `"Name"` or `"currentStateName"`, `"Current State Name"`
     *
     * The ID should be appended to a base Node ID
     * e.g. `state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId`
     */
    abstract fun getNodePropertyIdLabels(map: MutableMap<String, String> = mutableMapOf()): Map<String, String>

    /**
     * Returns Touch Portal ID and current Value of each Property of the node - id, value
     *
     * e.g. `"name"`, `"Node 1"` or `"currentStateName"`, `"Avatar 1"`
     *
     * The ID should be appended to a base Node ID
     * e.g. `state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId`
     */
    abstract fun getNodePropertyIdValues(map: MutableMap<String, String> = mutableMapOf()): Map<String, String>

    /**
     * Get End of this Node's Touch Portal State IDs
     *
     * e.g. name, id
     *
     *
     * These should be appended to a base Node ID (not done by this function)
     * e.g. `state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId`
     */
    abstract fun getNodeDataStateIds(): List<InstanceStateIdDescription>

    /**
     * Get End/Right Side of Node Touch Portal State IDs Mapped vs their values
     *
     * e.g. name, id
     *
     * These should be appended to a base Node ID
     * e.g. `state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId`
     */
    abstract fun getNodeDataValues(): Map<String, String>

    /**
     * Returns Map of IDs vs Labels for this Node Type
     * Usually just returns static nodeTPStateIDDescriptionMap
     */
    abstract fun getNodeTPStateIDLabelMap():Map<String, String>

    override fun toString(): String {
        return "VeadoNodeData(id='$id', type='$type', name='$name')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VeadoNodeData

        if (id != other.id) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    /** Inline function for actions based on Class inheriting [VeadoNodeData].
     *
     * @param stateNodeDataAction Action when VeadoStateNodeData
     * @param booleanNodeDataAction Action when VeadoBooleanNodeData
     * @param numberNodeDataAction Action VeadoNumberNodeData
     * @param otherAction Action when not one of the above Classes, passed as [VeadoNodeData]
     */
    inline fun perNodeDataAction(
        stateNodeDataAction: (VeadoStateNodeData) -> Unit,
        booleanNodeDataAction: (VeadoBooleanNodeData) -> Unit,
        numberNodeDataAction: (VeadoNumberNodeData) -> Unit,
        otherAction: (VeadoNodeData) -> Unit = {}
    ) {
        when (this) {
            is VeadoStateNodeData -> stateNodeDataAction(this)
            is VeadoBooleanNodeData -> booleanNodeDataAction(this)
            is VeadoNumberNodeData -> numberNodeDataAction(this)
            else -> otherAction(this)
        }
    }

    /** Inline function for actions based on instance type ([VeadoNodeData.type])
     *
     * `nodeType` e.g. 'stateEvents'/'boolean'/'number'
     *
     * @param stateEventsAction Action when Type = 'stateEvents'
     * @param booleanAction Action when Type = 'boolean'
     * @param numberAction Action when Type = 'number'
     * @param otherAction Action when Type is not above. If not set, throws an IllegalArgumentException
     *
     * @throws IllegalArgumentException if Type Not recognised and [otherAction] is not set
     */
    @Throws(IllegalArgumentException::class)
    internal inline fun <T> VeadoNodeData.perNodeTypeAction(
        stateEventsAction: (nodeType: String) -> T,
        booleanAction: (nodeType: String) -> T,
        numberAction: (nodeType: String) -> T,
        otherAction: (nodeType: String) -> T = { throw IllegalArgumentException("Unknown Node Type: $it") }
    ): T {
        return when (val nodeType = this.type) {
            "stateEvents" -> stateEventsAction(nodeType)
            "boolean" -> booleanAction(nodeType)
            "number" -> numberAction(nodeType)
            else -> otherAction(nodeType)
        }
    }

}



