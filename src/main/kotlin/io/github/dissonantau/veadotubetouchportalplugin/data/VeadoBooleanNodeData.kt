package io.github.dissonantau.veadotubetouchportalplugin.data

import io.github.dissonantau.bleatkan.message.ResultMessage


@Suppress("unused")
class VeadoBooleanNodeData(id: String, name: String, type: String = "boolean") : VeadoNodeData(id, name, type) {

    companion object {
        /**
         * Node Map of Fixed TP Data IDs.
         *
         * These are the fixed/Core IDs that always exist and don't change
         * - no Dynamic State IDs are kept here (e.g. Values of non-active states)
         */
        @JvmStatic
        val BooleanNodeTPStateIDLabelMap: Map<String, String> = mapOf(
            //tpStateNodeIdVeadoId to tpStateNodeDescriptionVeadoId,
            //tpStateNodeIdType to tpStateNodeDescriptionType,
            tpStateNodeIdName to tpStateNodeDescriptionName,

            tpStateNodeIdValue to tpStateNodeDescriptionValue
        )

        /** Touch Portal State ID for Veado Node Boolean Value*/
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdValue; get() = "value"

        /** Touch Portal State Description for Veado Node Boolean Value */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionValue; get() = "Value"
    }

    /** Value of Boolean Node*/
    var value: Boolean = false
        private set

    /**
     * Updates Node Value
     *
     * @return Previous Node Value
     */
    fun updateNodeValue(newValue: Boolean): Boolean {
        val oldValue = value
        value = newValue
        return oldValue
    }

    /**
     * Updates Node Value
     *
     * @return Previous Node Value
     */
    fun updateNodeValue(newValue: ResultMessage.ResultMessageWithPayloadBoolean): Boolean {
        val oldValue = value
        value = newValue.payload
        return oldValue
    }

    override fun toString(): String {
        return "VeadoBooleanNodeData(id='$id', type='$type', name='$name', value=$value)"
    }

    private val _nodeDataStateIds: MutableList<InstanceStateIdDescription> = mutableListOf()

    /**
     * Get End/Right Side of Node Touch Portal State IDs
     *
     * e.g. <nodeId>.name, <nodeId>.currentStateId, <nodeId>.states.<stateId>.id
     *
     * These should be appended to a base Node ID
     * e.g. state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId
     */
    override fun getNodeDataStateIds(): List<InstanceStateIdDescription> {
        if (_nodeDataStateIds.isEmpty()) {
            _nodeDataStateIds.add(InstanceStateIdDescription(tpStateNodeIdVeadoId, tpStateNodeDescriptionVeadoId))
            _nodeDataStateIds.add(InstanceStateIdDescription(tpStateNodeIdName, tpStateNodeDescriptionName))
            _nodeDataStateIds.add(InstanceStateIdDescription(tpStateNodeIdType, tpStateNodeDescriptionType))

            _nodeDataStateIds.add(InstanceStateIdDescription(tpStateNodeIdValue, tpStateNodeDescriptionValue))
        }
        return _nodeDataStateIds.toList()
    }

    /**
     * Get End/Right Side of Node Touch Portal State IDs Mapped vs their values
     *
     * e.g. <nodeId>.name, <nodeId>.currentStateId, <nodeId>.states.<stateId>.id
     *
     * These should be appended to a base Node ID
     * e.g. state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId
     */
    override fun getNodeDataValues(): Map<String, String> {
        val stateIdNodeValues = mutableMapOf<String, String>()

        stateIdNodeValues[tpStateNodeIdVeadoId] = id
        stateIdNodeValues[tpStateNodeIdName] = name
        stateIdNodeValues[tpStateNodeIdType] = type
        stateIdNodeValues[tpStateNodeIdValue] = value.toString()

        return stateIdNodeValues
    }

    override fun getNodeTPStateIDLabelMap(): Map<String, String> {
        return BooleanNodeTPStateIDLabelMap
    }

    inline fun inlineGetPropertyTpStateIdValue(postProcessActions: (id: String, value: String) -> Unit) {
        //postProcessActions(tpStateNodeIdVeadoId, id)
        //postProcessActions(tpStateNodeIdType, type)
        postProcessActions(tpStateNodeIdName, name)

        postProcessActions(tpStateNodeIdValue, value.toString())
    }

    inline fun inlineGetPropertyTpStateIdLabel(postProcessActions: (id: String, label: String) -> Unit) {
        //postProcessActions(tpStateNodeIdVeadoId, tpStateNodeDescriptionVeadoId)
        //postProcessActions(tpStateNodeIdType, tpStateNodeDescriptionType)
        postProcessActions(tpStateNodeIdName, tpStateNodeDescriptionName)

        postProcessActions(tpStateNodeIdValue, tpStateNodeDescriptionValue)
    }

    inline fun inlineGetPropertyTpStateIdLabelValue(postProcessActions: (id: String, label: String, value: String?) -> Unit) {
        //postProcessActions(tpStateNodeIdVeadoId, tpStateNodeDescriptionVeadoId, id)
        //postProcessActions(tpStateNodeIdType, tpStateNodeDescriptionType, type)
        postProcessActions(tpStateNodeIdName, tpStateNodeDescriptionName, name)

        postProcessActions(tpStateNodeIdValue, tpStateNodeDescriptionValue, value.toString())
    }

    override fun getNodePropertyIdLabels(map: MutableMap<String, String>): Map<String, String> {
        inlineGetPropertyTpStateIdLabel { id, label -> map[id] = label }
        return map
    }

    override fun getNodePropertyIdValues(map: MutableMap<String, String>): Map<String, String> {
        inlineGetPropertyTpStateIdValue { id, value -> map[id] = value }
        return map
    }
}