package io.github.dissonantau.veadotubetouchportalplugin.data

import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPluginConstants
import io.github.dissonantau.bleatkan.connection.Connection
import io.github.dissonantau.bleatkan.message.ResultMessage
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadState as BleatkanStatePeek
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadStateList as BleatkanStateList
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadPng as BleatkanStateThumbnail

@Suppress("unused")
class VeadoMiniConnectionData(connection: Connection, instanceNumber: Int) :
    VeadoConnectionData(connection, instanceNumber) {

    /** Instance title used during past refresh - used for comparisons */
    private var instanceTitle = ""

    /**
     * Generated State ID for veadotube mini's Current Avatar Thumbnail
     *
     * Excludes base section, used for updating State
     *
     * e.g. myTitle.currentAvatarStateThumbnail
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDTitledCurrentAvatarThumbnail: InstanceStateIdDescription = InstanceStateIdDescription("", "")
        private set

    /**
     * Generated State ID for veadotube mini's Push To Talk
     *
     * Excludes base section, used for updating State
     *
     * e.g. myTitle.pushToTalk
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDTitledPushToTalk: InstanceStateIdDescription = InstanceStateIdDescription("", "")
        private set

    /**
     * All Generated State ID for veadotube's Nodes and relevant data
     *
     * e.g. myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val currentInstanceTitleStateIDs: Set<InstanceStateIdDescription>
        get() = _currentInstanceTitleStateIDs.toSet()

    private var _currentInstanceTitleStateIDs: MutableSet<InstanceStateIdDescription>

    /**
     * All Generated State ID for veadotube's Nodes and relevant data
     *
     * e.g. myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val currentInstanceNumberedStateIDs: Set<InstanceStateIdDescription>
        get() = _currentInstanceNumberedStateIDs.toSet()

    private var _currentInstanceNumberedStateIDs: MutableSet<InstanceStateIdDescription>

    /**
     * Generated State ID for veadotube mini's Window Title
     *
     * Excludes base section, used for updating State
     *
     * e.g. myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDTitledInstanceTitle: InstanceStateIdDescription = InstanceStateIdDescription("", "")
        private set

    /** Default Mini Avatars Node */
    val nodeAvatars = VeadoStateNodeData("mini", "avatar state", "stateEvents")

    /** Default Mini push-to-talk/mic Node */
    val nodePushToTalk = VeadoBooleanNodeData("mini", "push-to-talk", "boolean")

    init {
        _currentInstanceTitleStateIDs = mutableSetOf()
        _currentInstanceNumberedStateIDs = mutableSetOf()

        /** Generate Instance Titles */
        refreshInstanceTitle()
        refreshInstanceNumber()
    }

    /**
     * ArrayList of all [VeadoState] available
     *
     * In order received from API, so they should be in the order they appear in Veadotube
     *
     * IDs are unique but Names can be duplicates.
     *
     * Work with this should probably be synchronised using the connection obj
     */
    val statesAll: ArrayList<VeadoState>; get() = nodeAvatars.statesAll

    /**
     * Get [VeadoState] from this connection by State ID
     *
     * ID = Name from Mini 2.1
     */
    fun getStateByID(stateID: String): VeadoState? = nodeAvatars.getStateByID(stateID)

    /**
     * Get [VeadoState] from this connection by State Name
     *
     * - if an exact match is found, it is chosen
     * - If no exact match is found, the first result that that contains the give name is returned (ignoreCase is used here)
     * - if none are found, null is returned
     *
     */
    fun getStateByNameContains(stateName: String, ignoreCase: Boolean = false): VeadoState? =
        nodeAvatars.getStateByNameContains(stateName, ignoreCase)

    /**
     * Pair with the current Avatar State with `ID` and `Name`
     *
     * Names can be duplicates, so the Current state may not match a value in the collectionStates
     *
     * If there's no match, it should request a new list in case a new value was added since list was fetched
     */
    val currentState: VeadoState?; get() = nodeAvatars.currentState

    /**
     * Current State Thumbnail
     *
     * Prevents deletion if several thumbnails are fetched and pushes the current state out of the LRU Maps
     */
    private val currentStateThumbnail: VeadoThumbnail?; get() = nodeAvatars.currentStateThumbnail

    /** Thumbnail Requests for Instance */
    var thumbnailRequestEnabled: Boolean = false

    /** Default Size of LRU Map - Hard References */
    private val lruHardMapSize = 4

    /** Default Size of LRU Map - Soft References*/
    private val lruSoftMapSize = lruHardMapSize + 6


    /* StateID Strings for Mini Instances */
    /**
     * Base State ID used for Dynamically creating and deleting States
     *
     * Excludes trailing dot (.)
     *
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state
     */
    override val baseStateID = "${VeadoTouchPluginConstants.MiniInstances.ID}.state"

    /**
     * Generated State ID for veadotube mini's Window Title
     *
     * Includes Base Section, used for Dynamically creating and deleting States
     *
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val stateIDTitledInstanceTitleLong; get() = "${baseStateID}.${stateIDTitledInstanceTitle.stateId}"

    /**
     * Generated State ID for veadotube mini's Current Avatar Name
     *
     * Includes Base Section, used for Dynamically creating and deleting States
     *
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.myTitle.currentAvatarStateName
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val stateIDTitledCurrentAvatarNameLong; get() = "${baseStateID}.${stateIDTitledCurrentAvatarName.stateId}"

    /**
     * Generated State ID for veadotube mini's Current Avatar Name
     *
     * Excludes base section, used for updating State
     *
     * e.g. myTitle.currentAvatarStateName
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDTitledCurrentAvatarName: InstanceStateIdDescription = InstanceStateIdDescription("", "")
        private set

    /**
     * Generated State ID for veadotube mini's Current Avatar Thumbnail
     *
     * Includes Base Section, used for Dynamically creating and deleting States
     *
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.myTitle.currentAvatarStateThumbnail
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val stateIDTitledCurrentAvatarThumbnailLong; get() = "${baseStateID}.${stateIDTitledCurrentAvatarThumbnail.stateId}"

    /**
     * Checks if Instance Title matches last processed, updates Title Cleaned & Simplified.
     *
     * @return true if title was updated, false if not
     */
    fun refreshInstanceTitleCalc(): Boolean {
        if (instanceTitle != instance.title || instanceTitle.isBlank()) {
            val firstDash = instance.title.indexOf('-')
            val newInstanceTitleCleaned = if (firstDash >= 0) {
                instance.title.substring(firstDash + 1).trim()
            } else {
                "${instance.id.type}${instance.id.process}"
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
     *
     * [instanceTitleCleaned], and State IDs that use [Instance.title][io.github.dissonantau.bleatkan.instance.Instance.title] are also Updated.
     *
     * [instanceTitleSimplified] only contains Letters, Digits, Dashes('-'), and Underscores ('_') from the title following the first dash after the program name.
     *
     * If nothing exists after the first Dash in the Title, "[InstanceID.type][io.github.dissonantau.bleatkan.instance.InstanceID.type]-[InstanceID.process][io.github.dissonantau.bleatkan.instance.InstanceID.process]" is used instead.
     *
     * @return [Pair] with 2 [Set]s - Old State IDs Removed, and New State IDs Added
     */
    @Suppress("MemberVisibilityCanBePrivate")
    override fun refreshInstanceTitle(): Pair<Set<InstanceStateIdDescription>, Set<InstanceStateIdDescription>> {
        refreshInstanceTitleCalc()

        val tpIdPrefix = instanceTitleSimplified
        val tpLabelPrefix = "Instance $instanceTitleCleaned (#$instanceNumber)"

        // List to put new IDs
        val listNewIDs: MutableSet<InstanceStateIdDescription> = mutableSetOf()

        stateIDTitledInstanceTitle = InstanceStateIdDescription("$tpIdPrefix.title", "$tpLabelPrefix: Title")
        listNewIDs.add(stateIDTitledInstanceTitle)

        stateIDTitledCurrentAvatarName =
            InstanceStateIdDescription("$tpIdPrefix.currentAvatarStateName", "$tpLabelPrefix: Current Avatar Name")
        listNewIDs.add(stateIDTitledCurrentAvatarName)
        stateIDTitledCurrentAvatarThumbnail =
            InstanceStateIdDescription(
                "$tpIdPrefix.currentAvatarStateThumbnail", "$tpLabelPrefix: Current Avatar Thumbnail"
            )
        listNewIDs.add(stateIDTitledCurrentAvatarThumbnail)
        stateIDTitledPushToTalk =
            InstanceStateIdDescription(
                "$tpIdPrefix.currentPushToTalkMicInput", "$tpLabelPrefix: Push-to-Talk Mic Input"
            )
        listNewIDs.add(stateIDTitledPushToTalk)

        // avatar state/nodeAvatars
        nodeAvatars.also { node ->
            generateNodeDataStateIds(tpIdPrefix, tpLabelPrefix, node)
            { listNewIDs.add(it); }
        }

        // push-to-talk/mic nodePushToTalk
        nodePushToTalk.also { node ->
            generateNodeDataStateIds(tpIdPrefix, tpLabelPrefix, node)
            { listNewIDs.add(it); }
        }

        val currentIds = _currentInstanceTitleStateIDs
        val addedIds = listNewIDs.subtract(currentIds)
        val removedIds = currentIds.subtract(listNewIDs)

        _currentInstanceTitleStateIDs = listNewIDs

        return Pair(removedIds, addedIds)
    }

    /**
     * Updates State IDs that use [instanceNumber]
     *
     * @return List of the previous State IDs for Removal
     * @throws IllegalStateException If [instanceNumber] has not been set
     */
    @Suppress("MemberVisibilityCanBePrivate")
    override fun refreshInstanceNumber(): Pair<Set<InstanceStateIdDescription>, Set<InstanceStateIdDescription>> {
        check(instanceNumber > 0) { "Instance Number has not been set" }

        val tpIdPrefix = instanceNumber.toString()
        val tpLabelPrefix = "Instance Mini #$instanceNumber"

        // List old IDs and Generate new ones
        val listNewIDs: MutableSet<InstanceStateIdDescription> = mutableSetOf()

        stateIDNumberedInstanceTitle = InstanceStateIdDescription("$tpIdPrefix.title", "$tpLabelPrefix: Title")
        listNewIDs.add(stateIDNumberedInstanceTitle)

        stateIDNumberedCurrentAvatarName =
            InstanceStateIdDescription("$tpIdPrefix.currentAvatarStateName", "$tpLabelPrefix: Current Avatar Name")
        listNewIDs.add(stateIDNumberedCurrentAvatarName)
        stateIDNumberedCurrentAvatarThumbnail =
            InstanceStateIdDescription(
                "$tpIdPrefix.currentAvatarStateThumbnail", "$tpLabelPrefix: Current Avatar Thumbnail"
            )
        listNewIDs.add(stateIDNumberedCurrentAvatarThumbnail)
        stateIDNumberedPushToTalk =
            InstanceStateIdDescription(
                "$tpIdPrefix.currentPushToTalkMicInput", "$tpLabelPrefix: Push-to-Talk Mic Input"
            )
        listNewIDs.add(stateIDNumberedPushToTalk)

        // avatar state/nodeAvatars
        nodeAvatars.also { node ->
            generateNodeDataStateIds(tpIdPrefix, tpLabelPrefix, node)
            { listNewIDs.add(it); }
        }

        // push-to-talk/mic nodePushToTalk
        nodePushToTalk.also { node ->
            generateNodeDataStateIds(tpIdPrefix, tpLabelPrefix, node)
            { listNewIDs.add(it); }
        }

        val currentIds = _currentInstanceNumberedStateIDs
        val addedIds = listNewIDs.subtract(currentIds)
        val removedIds = currentIds.subtract(listNewIDs)

        _currentInstanceNumberedStateIDs = listNewIDs

        return Pair(removedIds, addedIds)
    }

    /**
     * Clears [currentInstanceNumberedStateIDs] and returns the Node State IDs
     *
     * @see refreshInstanceNumber
     * @return [Set] - State IDs Removed
     */
    override fun clearInstanceNumber(): Set<InstanceStateIdDescription> {
        val currentIds = _currentInstanceNumberedStateIDs
        _currentInstanceNumberedStateIDs = mutableSetOf()
        return currentIds
    }

    /**
     * Clears [currentInstanceTitleStateIDs] and returns removed IDs for processing
     *
     * @see refreshInstanceTitle
     * @return [Set] - Old State IDs
     */
    @Suppress("MemberVisibilityCanBePrivate")
    override fun clearInstanceTitle(): Set<InstanceStateIdDescription> {
        val currentIds = _currentInstanceTitleStateIDs
        _currentInstanceTitleStateIDs = mutableSetOf()
        return currentIds
    }

    private inline fun allNodes(nodeActions: (VeadoNodeData) -> Unit = {}) {
        nodeActions(nodeAvatars)
        nodeActions(nodePushToTalk)
    }

    override fun getInstanceNodePropertyNumberValues(map: MutableMap<String, String>): Map<String, String> {
        val tpIdPrefix = instanceNumber.toString()
        map["$tpIdPrefix.title"] = instanceTitleCleaned
        map["$tpIdPrefix.currentAvatarStateName"] = currentState?.name ?: ""
        map["$tpIdPrefix.currentAvatarStateThumbnail"] = currentState?.thumbnail?.png ?: ""
        map["$tpIdPrefix.currentPushToTalkMicInput"] = getPushToTalkString()

        allNodes { node ->
            generateNodePropertyIdValue(nodePrefix = tpIdPrefix, node = node)
            { id, value -> map[id] = value }
        }
        return map
    }

    override fun getInstanceNodePropertyTitleValues(map: MutableMap<String, String>): Map<String, String> {
        val tpIdPrefix = instanceTitleSimplified
        map["$tpIdPrefix.title"] = instanceTitleCleaned
        map["$tpIdPrefix.currentAvatarStateName"] = currentState?.name ?: ""
        map["$tpIdPrefix.currentAvatarStateThumbnail"] = currentState?.thumbnail?.png ?: ""
        map["$tpIdPrefix.currentPushToTalkMicInput"] = getPushToTalkString()

        allNodes { node ->
            generateNodePropertyIdValue(nodePrefix = tpIdPrefix, node = node)
            { id, value -> map[id] = value }
        }
        return map
    }

    override fun getInstanceNodePropertyValues(map: MutableMap<String, String>): Map<String, String> {
        allNodes { node ->
            generateNodePropertyIdValueMulti(node = node) { id, value -> map[id] = value }
        }
        return map
    }

    /**
     * Generated State ID for mini Window Title
     *
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.1.currentAvatarStateName
     * */
    @Suppress("MemberVisibilityCanBePrivate")
    val stateIDNumberedInstanceTitleLong; get() = "${baseStateID}.${stateIDNumberedInstanceTitle.stateId}"

    /**
     * Generated State ID for mini Windows Title
     *
     * Excludes base section, used for updating State
     *
     * e.g. myTitle.title
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDNumberedInstanceTitle = InstanceStateIdDescription("", "")
        private set

    /**
     * Generated State ID for mini Current Avatar Name
     *
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.1.currentAvatarStateName
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val stateIDNumberedCurrentAvatarNameLong; get() = "${baseStateID}.${stateIDNumberedCurrentAvatarName.stateId}"

    /**
     * Generated State ID for mini Windows Title
     *
     * Excludes base section, used for updating State
     *
     * e.g. myTitle.currentAvatarStateName
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDNumberedCurrentAvatarName = InstanceStateIdDescription("", "")
        private set

    /**
     * Generated State ID for mini Current Avatar Name
     *
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.1.currentAvatarStateThumbnail
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val stateIDNumberedCurrentAvatarThumbnailLong; get() = "${baseStateID}.${stateIDNumberedCurrentAvatarThumbnail.stateId}"

    /**
     * Generated State ID for veadotube mini's Push To Talk
     *
     * Excludes base section, used for updating State
     *
     * e.g. myTitle.pushToTalk
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDNumberedPushToTalk: InstanceStateIdDescription = InstanceStateIdDescription("", "")
        private set

    /**
     * Generated State ID for mini Avatar Thumbnail
     *
     * Excludes base section, used for updating State
     *
     * e.g. myTitle.currentAvatarStateThumbnail
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var stateIDNumberedCurrentAvatarThumbnail = InstanceStateIdDescription("", "")
        private set

    /**
     * Replaces all states
     *
     * If the list's don't match, items are checked and updated
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun updateStates(payload: BleatkanStateList) = nodeAvatars.updateStates(payload)

    /**
     * Update Current State
     *
     * @param stateID ID from Peek/Listen Result Message
     *
     * @return Whether state was updated - false means the state is already the one provided
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun updateCurrentState(stateID: BleatkanStatePeek): Boolean = nodeAvatars.updateCurrentState(stateID)

    /** Clear currentStateThumbnail, Maps, etc. */
    @Suppress("MemberVisibilityCanBePrivate")
    fun clearedStateThumbnail(veadoState: VeadoState) = nodeAvatars.clearedStateThumbnail(veadoState)

    /** Updates a State Thumbnail, creating the state if it doesn't exist
     *
     * @param payload Payload with State Thumbnail Data to update
     * @param updateToMRU forces the State Thumbnail to be made the *Most Recently Used* on in the Soft and Hard Thumbnail Maps
     *
     * @return Pair: First = Boolean, if thumbnail was Updated; Second = VeadoState Updated, Third = VeadoThumbnail?, thumbnail object if updated
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun updateStateThumbnail(
        payload: BleatkanStateThumbnail, updateToMRU: Boolean = false
    ): Triple<Boolean, VeadoState, VeadoThumbnail?> = nodeAvatars.updateStateThumbnail(payload, updateToMRU)

    /** Update Push-To-Talk node value. Returns previous Node Value.
     * @see [VeadoBooleanNodeData.updateNodeValue] */
    fun updatePushToTalk(newValue: Boolean): Boolean = nodePushToTalk.updateNodeValue(newValue)

    /** Update Push-To-Talk node value. Returns previous Node Value.
     * @see [VeadoBooleanNodeData.updateNodeValue] */
    @Suppress("NOTHING_TO_INLINE")
    inline fun updatePushToTalk(payload: ResultMessage.ResultMessageWithPayloadBoolean): Boolean =
        updatePushToTalk(payload.payload)

    /** Mini PTT state as Boolean as a String - True = 'Unmuted', False = 'Muted' */
    @Suppress("NOTHING_TO_INLINE")
    inline fun getPushToTalkString() = if (nodePushToTalk.value) "Unmuted" else "Muted"

    /** Mini PTT state as Boolean - True = Unmuted, False = Muted */
    @Suppress("NOTHING_TO_INLINE")
    inline fun getPushToTalk() = nodePushToTalk.value

    /**
     * Generates the ID and Label for a connection's Push To Talk node and passes to action lambdas
     *
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     */
    inline fun getTPStateIdLabelForPushToTalkNode(
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit
    ) = getTPStateIdLabelForNode(nodePushToTalk, instanceTPStateIdNumber, instanceTPStateIdTitle)

    /**
     * Generates the ID and Label for a connection's Push To Talk node Property and passes to action lambdas
     *
     * @param propertyId ID of Property - e.g. id, name
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     * @param actionValueIdNotFound Action executed if Value ID isn't found in Label Map
     */
    inline fun getTPStateIdLabelForPushToTalkProperty(
        propertyId: String,
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        actionValueIdNotFound: () -> Unit = {}
    ) = getTPStateIdLabelNodeProperty(
        nodePushToTalk, propertyId, instanceTPStateIdNumber, instanceTPStateIdTitle, actionValueIdNotFound
    )

    /**
     * Generates the ID and Label for a connection's Avatars node and passes to action lambdas
     *
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     */
    inline fun getTPStateIdLabelForAvatarsNode(
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit
    ) = getTPStateIdLabelForNode(nodeAvatars, instanceTPStateIdNumber, instanceTPStateIdTitle)

    /**
     * Generates the ID and Label for a connection's Avatars node value and passes to action lambdas
     *
     * @param propertyId ID of Value - e.g. id, name
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     */
    inline fun getTPStateIdLabelForAvatarsProperty(
        propertyId: String,
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit
    ) = getTPStateIdLabelNodeProperty(nodeAvatars, propertyId, instanceTPStateIdNumber, instanceTPStateIdTitle)

    /**
     * Generates the ID and Label for a connection's Push To Talk [VeadoBooleanNodeData.tpStateNodeIdValue] and passes to action lambdas
     *
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     */
    inline fun getTPStateIdLabelPushToTalkValue(
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit
    ) = getTPStateIdLabelNodeProperty(
        nodePushToTalk, VeadoBooleanNodeData.tpStateNodeIdValue, instanceTPStateIdNumber, instanceTPStateIdTitle
    )

    /**
     * Generates the ID and Label for a connection's Push To Talk [VeadoNodeData.tpStateNodeIdVeadoId] and passes to action lambdas
     *
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     */
    inline fun getTPStateIdLabelPushToTalkVeadoId(
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit
    ) = getTPStateIdLabelNodeProperty(
        nodePushToTalk, VeadoNodeData.tpStateNodeIdVeadoId, instanceTPStateIdNumber, instanceTPStateIdTitle
    )

    /**
     * Generates the ID and Label for a connection's Push To Talk [VeadoNodeData.tpStateNodeIdName] and passes to action lambdas
     *
     * @param instanceTPStateIdNumber ID and Label for TP Node by Number `<InstanceTitle>.nodes.<type>.<nodeId>.<stateId>`
     * @param instanceTPStateIdTitle ID and Label for TP Node by Title `<InstanceNumber>.nodes.<type>.<nodeId>.<stateId>`
     */
    inline fun getTPStateIdLabelPushToTalkVeadoName(
        instanceTPStateIdNumber: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit,
        instanceTPStateIdTitle: (instanceTPStateId: String, instanceTPStateLabel: String) -> Unit
    ) = getTPStateIdLabelNodeProperty(
        nodePushToTalk, VeadoNodeData.tpStateNodeIdName, instanceTPStateIdNumber, instanceTPStateIdTitle
    )

}