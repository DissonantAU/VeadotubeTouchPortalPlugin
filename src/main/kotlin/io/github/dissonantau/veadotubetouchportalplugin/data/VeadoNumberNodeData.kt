package io.github.dissonantau.veadotubetouchportalplugin.data


@Suppress("unused", "MemberVisibilityCanBePrivate")
class VeadoNumberNodeData(id: String, name: String, type: String = "number") : VeadoNodeData(id, name, type) {

    companion object {
        /**
         * Node Map of Fixed TP Data IDs.
         *
         * These are the fixed/Core IDs that always exist and don't change
         * - no Dynamic State IDs are kept here (e.g. Values of non-active states)
         */
        @JvmStatic
        val NumberNodeTPStateIDLabelMap: Map<String, String> = mapOf(
            //tpStateNodeIdVeadoId to tpStateNodeDescriptionVeadoId,
            //tpStateNodeIdType to tpStateNodeDescriptionType,
            tpStateNodeIdName to tpStateNodeDescriptionName,

            tpStateNodeIdValue to tpStateNodeDescriptionValue,
            tpStateNodeIdMinimum to tpStateNodeDescriptionMinimum,
            tpStateNodeIdMaximum to tpStateNodeDescriptionMaximum
        )

        /** Touch Portal State ID for Veado Node Number Value */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdValue; get() = "value"

        /** Touch Portal State Description for Veado Node Number Value */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionValue; get() = "Value"

        /** Touch Portal State ID for Veado Node Number Value Minimum */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdMinimum; get() = "minimum"

        /** Touch Portal State Description for Veado Node Number Minimum Value */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionMinimum; get() = "Minimum"

        /** Touch Portal State ID for Veado Node Number Value Maximum */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdMaximum; get() = "maximum"

        /** Touch Portal State Description for Veado Node Number Maximum Value */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionMaximum; get() = "Maximum"
    }


    /** Value of Number Node - Defaults to 0.0 */
    @Suppress("MemberVisibilityCanBePrivate")
    var value: Double = 0.0; private set

    /** Minimum of Number Node - Defaults to [Double.NEGATIVE_INFINITY] if not set */
    @Suppress("MemberVisibilityCanBePrivate")
    var minimum: Double = Double.NEGATIVE_INFINITY; private set

    /** If Minimum Value is Set */
    var minimumSet = false; private set


    /** Maximum of Number Node - Defaults to [Double.POSITIVE_INFINITY] if not set */
    @Suppress("MemberVisibilityCanBePrivate")
    var maximum: Double = Double.POSITIVE_INFINITY; private set

    /** If Maximum Value is Set */
    var maximumSet = false; private set

    /**
     * Updates Node Value
     *
     * @return Previous Node Value
     */
    fun updateNodeValue(newValue: Double): Double {
        val oldValue = value
        value = newValue
        return oldValue
    }

    /**
     * Updates Node Minimum Value
     *
     * Null or [Double.NEGATIVE_INFINITY] = not set
     * [minimumSet] is also updated
     *
     * @return Previous Node Value
     */
    fun updateNodeMin(newValue: Double?): Double {
        if (newValue == minimum) return minimum
        val oldValue = minimum
        if (newValue == null || newValue == Double.NEGATIVE_INFINITY) {
            minimum = Double.NEGATIVE_INFINITY; minimumSet = false
        } else {
            minimum = newValue; minimumSet = true
        }
        _nodeDataStateIds.clear()
        return oldValue
    }

    /**
     * Updates Node Maximum Value
     *
     * Null or [Double.POSITIVE_INFINITY] = not set
     * [maximumSet] is also updated
     *
     * @return Previous Node Value
     */
    fun updateNodeMax(newValue: Double?): Double {
        if (newValue == maximum) return maximum
        val oldValue = maximum
        if (newValue == null || newValue == Double.POSITIVE_INFINITY) {
            minimum = Double.POSITIVE_INFINITY; maximumSet = false
        } else {
            maximum = newValue; maximumSet = true
        }
        _nodeDataStateIds.clear()
        return oldValue
    }

    override fun toString(): String {
        return "VeadoNumberNodeData(id='$id', type='$type', name='$name', value=$value, minimum=${
            getMinimumAsStringOrElse { "Not Set" }
        }, maximum=${getMaximumAsStringOrElse { "Not Set" }})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as VeadoNumberNodeData

        if (value != other.value) return false
        if (minimum != other.minimum) return false
        if (maximum != other.maximum) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + minimum.hashCode()
        result = 31 * result + maximum.hashCode()
        return result
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Double.numberNodeDoubleToText() = this.toString().trimEnd('0').trimEnd('.')

    @Suppress("NOTHING_TO_INLINE")
    inline fun Double.numberNodeDoubleToText3d() = "%.3f".format(this).trimEnd('0').trimEnd('.')

    inline fun minimumIsSet(setAction: () -> Unit = { }, notSetAction: () -> Unit = { }) {
        return if (minimumSet) setAction() else notSetAction()
    }

    inline fun maximumIsSet(setAction: () -> Unit = {}, notSetAction: () -> Unit = {}) {
        return if (maximumSet) setAction() else notSetAction()
    }

    inline fun minimumIsSetValue(setAction: (minimum: Double) -> Unit = { }, notSetAction: () -> Unit = { }) {
        return if (minimumSet) setAction(minimum) else notSetAction()
    }

    inline fun maximumIsSetValue(setAction: (maximum: Double) -> Unit = {}, notSetAction: () -> Unit = {}) {
        return if (maximumSet) setAction(maximum) else notSetAction()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun getValueAsString(): String {
        return value.numberNodeDoubleToText()
    }

    inline fun getMinimumAsStringOrElse(elseAction: () -> String = { "" }): String {
        return if (minimumSet) minimum.numberNodeDoubleToText() else elseAction()
    }

    inline fun getMaximumAsStringOrElse(elseAction: () -> String = { "" }): String {
        return if (maximumSet) maximum.numberNodeDoubleToText() else elseAction()
    }


    inline fun ifMinimumAsStringOrElse(setAction: (minimum: String) -> Unit = { }, elseAction: () -> Unit = { }) {
        return if (minimumSet) setAction(minimum.numberNodeDoubleToText()) else elseAction()
    }

    inline fun ifMaximumAsStringOrElse(setAction: (maximum: String) -> Unit = { }, elseAction: () -> Unit = { }) {
        return if (maximumSet) setAction(maximum.numberNodeDoubleToText()) else elseAction()
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

            minimumIsSet({
                _nodeDataStateIds.add(
                    InstanceStateIdDescription(tpStateNodeIdMinimum, tpStateNodeDescriptionMinimum)
                )
            })
            maximumIsSet({
                _nodeDataStateIds.add(
                    InstanceStateIdDescription(tpStateNodeIdMaximum, tpStateNodeDescriptionMaximum)
                )
            })
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

        minimumIsSet({ stateIdNodeValues[tpStateNodeIdMinimum] = minimum.toString() })
        maximumIsSet({ stateIdNodeValues[tpStateNodeIdMaximum] = maximum.toString() })

        return stateIdNodeValues
    }

    override fun getNodeTPStateIDLabelMap(): Map<String, String> {
        return NumberNodeTPStateIDLabelMap
    }

    inline fun inlineGetPropertyTpStateIdValue(postProcessActions: (id: String, value: String?) -> Unit) {
        //postProcessActions(tpStateNodeIdVeadoId, id)
        //postProcessActions(tpStateNodeIdType, type)
        postProcessActions(tpStateNodeIdName, name)

        postProcessActions(tpStateNodeIdValue, value.toString())
        minimumIsSet(
            { postProcessActions(tpStateNodeIdMinimum, minimum.toString()) },
            { postProcessActions(tpStateNodeIdMinimum, null) }
        )
        maximumIsSet(
            { postProcessActions(tpStateNodeIdMaximum, maximum.toString()) },
            { postProcessActions(tpStateNodeIdMaximum, null) }
        )
    }

    inline fun inlineGetPropertyTpStateIdLabel(postProcessActions: (id: String, label: String) -> Unit) {
        //postProcessActions(tpStateNodeIdVeadoId, tpStateNodeDescriptionVeadoId)
        //postProcessActions(tpStateNodeIdType, tpStateNodeDescriptionType)
        postProcessActions(tpStateNodeIdName, tpStateNodeDescriptionName)

        postProcessActions(tpStateNodeIdValue, tpStateNodeDescriptionValue)

        postProcessActions(tpStateNodeIdMinimum, tpStateNodeDescriptionMinimum)
        postProcessActions(tpStateNodeIdMaximum, tpStateNodeDescriptionMaximum)
    }

    inline fun inlineGetPropertyTpStateIdLabelValue(postProcessActions: (id: String, label: String, value: String?) -> Unit) {
        //postProcessActions(tpStateNodeIdVeadoId, tpStateNodeDescriptionVeadoId, id)
        //postProcessActions(tpStateNodeIdType, tpStateNodeDescriptionType, type)
        postProcessActions(tpStateNodeIdName, tpStateNodeDescriptionName, name)

        postProcessActions(tpStateNodeIdValue, tpStateNodeDescriptionValue, value.toString())
        minimumIsSet(
            { postProcessActions(tpStateNodeIdMinimum, tpStateNodeDescriptionMinimum, minimum.toString()) },
            { postProcessActions(tpStateNodeIdMinimum, tpStateNodeDescriptionMinimum, null) }
        )
        maximumIsSet(
            { postProcessActions(tpStateNodeIdMaximum, tpStateNodeDescriptionMaximum, maximum.toString()) },
            { postProcessActions(tpStateNodeIdMaximum, tpStateNodeDescriptionMaximum, null) }
        )
    }

    override fun getNodePropertyIdLabels(map: MutableMap<String, String>): Map<String, String> {
        inlineGetPropertyTpStateIdLabel { id, label -> map[id] = label }
        return map
    }

    override fun getNodePropertyIdValues(map: MutableMap<String, String>): Map<String, String> {
        inlineGetPropertyTpStateIdValue { id, value -> map[id] = value ?: "" }
        return map
    }

}