package io.github.dissonantau.veadotubetouchportalplugin.data

import org.apache.commons.collections4.map.LRUMap
import java.lang.ref.SoftReference
import kotlin.collections.set

import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadState as BleatkanStatePeek
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadStateList as BleatkanStateList
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadPng as BleatkanStateThumbnail

@Suppress("unused")
class VeadoStateNodeData(id: String, name: String, type: String = "stateEvents") : VeadoNodeData(id, name, type) {

    companion object {
        /**
         * Node Map of Fixed TP Data IDs.
         *
         * These are the fixed/Core IDs that always exist and don't change
         * - no Dynamic State IDs are kept here (e.g. Values of non-active states)
         */
        @JvmStatic
        val StateNodeTPStateIDLabelMap: Map<String, String> = mapOf(
            //tpStateNodeIdVeadoId to tpStateNodeDescriptionVeadoId,
            //tpStateNodeIdType to tpStateNodeDescriptionType,
            tpStateNodeIdName to tpStateNodeDescriptionName,

            tpStateNodeIdCurrentStateId to tpStateNodeDescriptionCurrentStateId,
            tpStateNodeIdCurrentStateName to tpStateNodeDescriptionCurrentStateName,

            tpStateNodeIdCurrentStateThumbnailWidth to tpStateNodeDescriptionCurrentStateThumbnailWidth,
            tpStateNodeIdCurrentStateThumbnailHeight to tpStateNodeDescriptionCurrentStateThumbnailHeight,
            tpStateNodeIdCurrentStateThumbnailHash to tpStateNodeDescriptionCurrentStateThumbnailHash,
            tpStateNodeIdCurrentStateThumbnail to tpStateNodeDescriptionCurrentStateThumbnail
        )

        /** Touch Portal State ID for Veado Node currentStateId*/
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdCurrentStateId; get() = "currentStateId"

        /** Touch Portal State ID for  Veado Node currentStateName */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdCurrentStateName; get() = "currentStateName"

        /** Touch Portal State ID for Veado Node currentStateThumbnail */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdCurrentStateThumbnail; get() = "currentStateThumbnail"

        /** Touch Portal State ID for Veado Node currentStateThumbnailHash */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdCurrentStateThumbnailHash; get() = "currentStateThumbnailHash"

        /** Touch Portal State ID for Veado Node currentStateThumbnailWidth */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdCurrentStateThumbnailWidth; get() = "currentStateThumbnailWidth"

        /** Touch Portal State ID for Veado Node currentStateThumbnailHeight */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeIdCurrentStateThumbnailHeight; get() = "currentStateThumbnailHeight"

        /** Touch Portal State ID for Veado Node Current State ID */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionCurrentStateId; get() = "Current State ID"

        /** Touch Portal State ID for Veado Node Current State Name */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionCurrentStateName; get() = "Current State Name"

        /** Touch Portal State ID for Veado Node Current State Thumbnail */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionCurrentStateThumbnail; get() = "Current State Thumbnail"

        /** Touch Portal State ID for Veado Node Current State Thumbnail Hash */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionCurrentStateThumbnailHash; get() = "Current State Thumbnail Hash"

        /** Touch Portal State ID for Veado Node Current State Thumbnail Width */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionCurrentStateThumbnailWidth; get() = "Current State Thumbnail Width"

        /** Touch Portal State ID for Veado Node Current State Thumbnail Height */
        @Suppress("MemberVisibilityCanBePrivate")
        inline val tpStateNodeDescriptionCurrentStateThumbnailHeight; get() = "Current State Thumbnail Height"


        inline val thumbnailPngIdSuffix; get() = ".thumbnailPng"
        inline val thumbnailHashIdSuffix; get() = ".thumbnailHash"
        inline val thumbnailWidthIdSuffix; get() = ".thumbnailWidth"
        inline val thumbnailHeightIdSuffix; get() = ".thumbnailHeight"
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
    var statesAll = ArrayList<VeadoState>()
        private set

    /**
     * Map of States - `ID` vs `VeadoState`
     *
     * Work with this should probably be synchronised using the connection obj
     */
    private val statesByID = HashMap<String, VeadoState>()

    /**
     * Pair with the current Avatar State with `ID` and `Name`
     *
     * Names can be duplicates, so the Current state may not match a value in the collectionStates
     *
     * If there's no match, it should request a new list in case a new value was added since list was fetched
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var currentState: VeadoState? = null
        private set(value) {
            stateIdNodeValuesClean = false
            field = value
        }

    /** Set if node should act as though auto get thumbnail is enabled. Set true if a thumbnail is assigned */
    var overrideEnableAutoGetThumbnail = false
        private set

    /**
     * Current State Thumbnail
     *
     * Prevents deletion if several thumbnails are fetched and pushes the current state out of the LRU Maps
     *
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var currentStateThumbnail: VeadoThumbnail? = null
        private set

    /** Default Size of LRU Map - Hard References */
    private val lruHardMapSize = 4

    /** Default Size of LRU Map - Soft References*/
    private val lruSoftMapSize = lruHardMapSize + 6

    /**
     * Least Recently Used Map with Hard References
     *
     * Will keep #[lruHardMapSize] last used values prevention deletion by GC
     *
     * See: [Apache Commons LRUMap](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/LRUMap.html)
     */
    private val thumbnailHardLRUMap: LRUMap<VeadoState, VeadoThumbnail> =
        LRUMap<VeadoState, VeadoThumbnail>(lruHardMapSize)

    /**
     * Least Recently Used Map with Soft References
     *
     * Will keep #[lruHardMapSize] + #[lruSoftMapSize] last used values to reduce chance of deletion by GC
     *
     * See: [Apache Commons LRUMap](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/LRUMap.html)
     */
    private val thumbnailSoftLRUMap: LRUMap<VeadoState, SoftReference<VeadoThumbnail>> =
        LRUMap<VeadoState, SoftReference<VeadoThumbnail>>(lruSoftMapSize)

    /**
     * Replaces all states
     *
     * If the list's don't match, items are checked and updated
     */
    fun updateStates(payload: BleatkanStateList): Boolean {
        LOGGER.trace { "updateStates: Begin" }

        val stateList = payload.states
        // New List
        val newAllList: ArrayList<VeadoState> = ArrayList(stateList.size)

        // Track if list has changed - Count, Names, etc. - We want to regenerate the list and send to Touch Portal
        var listReplace = false
        // If Count is different, mark as changed
        if (statesAll.count() != stateList.count()) listReplace = true

        for (i in stateList.indices) {

            val stateListNew = stateList[i]
            val stateListOld = statesAll.getOrNull(i)

            LOGGER.trace { "updateStates: stateList item ${i}; new ${stateListNew.id},${stateListNew.name}; old ${stateListOld?.id},${stateListOld?.name}" }

            val existingState: VeadoState =
                if (stateListOld != null && stateListNew.id == stateListOld.id) {
                    //Order Match, get state from array
                    LOGGER.trace { "updateStates: stateList item ${i}; new ${stateListNew.id} == old ${stateListOld.id}" }

                    stateListOld
                } else {
                    //Not the same, we need to check if it exists in HashMap and get, or create new one from new State
                    LOGGER.trace { "updateStates: stateList item ${i}; new ${stateListNew.id} != old ${stateListOld?.id}" }
                    LOGGER.trace { "updateStates: stateList item ${i}; statesByID[stateListNew.id] == old ${statesByID[stateListNew.id]?.id},${statesByID[stateListNew.id]?.name}}" }
                    listReplace = true
                    LOGGER.trace { "updateStates: New VeadoState > listReplace = $listReplace" }

                    statesByID.getOrPut(stateListNew.id) {
                        LOGGER.trace { "updateStates: create VeadoState for ${stateListNew.id}" }
                        VeadoState(stateListNew).also {
                            LOGGER.trace { "updateStates: stateList item ${i}; new item ${it.id},${it.name}" }
                        }

                    }
                }

            if (existingState.name != stateListNew.name) {
                // Update Object if the doesn't match
                existingState.update(stateListNew)
                listReplace = true
                LOGGER.trace { "updateStates: Update Name > listReplace = $listReplace" }
            }

            LOGGER.trace { "updateStates: stateList item ${i}; add to new list ${existingState.id},${existingState.name}" }

            //Add to new List if we need to
            newAllList.add(existingState)

        }
        LOGGER.trace { "updateStates: Post Loop listReplace = $listReplace" }

        //If List changed (order, new items, etc.)
        if (listReplace) {
            // Replace Old List
            statesAll = newAllList

            // Update Maps - converts statesAll to HashSet for performance
            val tempHashSet = statesAll.toCollection(HashSet())
            statesByID.values.retainAll(tempHashSet)
            thumbnailHardLRUMap.keys.retainAll(tempHashSet)
            thumbnailSoftLRUMap.keys.retainAll(tempHashSet)

            stateIdNodeValuesClean = false
        }

        return listReplace
    }


    /**
     * Update Current State
     *
     * @param stateID ID from Peek/Listen Result Message
     *
     * @return Whether state was updated - false means the state is already the one provided
     */
    fun updateCurrentState(stateID: BleatkanStatePeek): Boolean {
        if (currentState?.id == stateID.state) {
            // Already matches
            return false
        } else {
            // Not a match
            val newCurrentState: VeadoState = statesByID.getOrPut(stateID.state) { VeadoState(stateID) }

            // Update and return
            currentState = newCurrentState
            stateIdNodeValuesClean = false
            return true
        }
    }


    /** Clear currentStateThumbnail, Maps, etc. */
    fun clearedStateThumbnail(veadoState: VeadoState): Boolean {
        val result = veadoState.clearThumbnail()

        if (currentState == veadoState) {
            currentStateThumbnail = null
        }

        thumbnailHardLRUMap.remove(veadoState)
        thumbnailSoftLRUMap.remove(veadoState)

        overrideEnableAutoGetThumbnail = false
        stateIdNodeValuesClean = false
        return result
    }


    /** Updates a State Thumbnail, creating the state if it doesn't exist
     *
     * @param payload Payload with State Thumbnail Data to update
     * @param updateToMRU forces the State Thumbnail to be made the *Most Recently Used* on in the Soft and Hard Thumbnail Maps
     *
     * @return Pair: First = Boolean, if thumbnail was Updated; Second = VeadoState Updated, Third = VeadoThumbnail?, thumbnail object if updated
     */
    fun updateStateThumbnail(
        payload: BleatkanStateThumbnail,
        updateToMRU: Boolean = false
    ): Triple<Boolean, VeadoState, VeadoThumbnail?> {
        // Get State, create if it doesn't exist (Thumbnail Payload is used, but Thumbnail info not used so se get accurate update response)
        val updateState: VeadoState = statesByID.getOrPut(payload.state) { VeadoState(payload) }

        // Update and return Success Boolean
        val updateThumbnailResult = updateState.updateThumbnail(payload)

        if (updateThumbnailResult) {
            val newThumbnail = updateState.thumbnail

            if (newThumbnail != null) {
                if (currentState == updateState) {
                    currentStateThumbnail = newThumbnail
                }

                // If in current Hard Map, object should have updated already
                if ((thumbnailHardLRUMap.get(updateState, updateToMRU) == null && updateToMRU)
                    || !thumbnailHardLRUMap.isFull
                ) {
                    // If NOT in list, and update to MRU is true OR Map isn't full - Add
                    thumbnailHardLRUMap[updateState] = newThumbnail
                }

                // If in current Soft Map, object should have updated already
                if ((thumbnailSoftLRUMap.get(updateState, updateToMRU)?.get() == null && updateToMRU)
                    || !thumbnailSoftLRUMap.isFull
                ) {
                    // If NOT in list, and update to MRU is true OR Map isn't full - Add
                    thumbnailSoftLRUMap[updateState] = SoftReference(newThumbnail)
                }
            }

            overrideEnableAutoGetThumbnail = true
            stateIdNodeValuesClean = false
            // Return true (thumbnail updated) and null (thumbnail not of Current State)
            return Triple(true, updateState, newThumbnail)
        }
        // Return false (thumbnail not updated) and null (thumbnail not updated)
        return Triple(false, updateState, null)
    }


    /**
     * Get [VeadoState] from this connection by State ID
     *
     * ID = Name from Mini 2.1
     */
    fun getStateByID(stateID: String): VeadoState? = statesByID[stateID]


    /**
     * Get [VeadoState] from this Connection/Instance by State Name
     *
     * - if an exact match is found, it is chosen
     * - If no exact match is found, the first result that that contains the give name is returned (ignoreCase is used here)
     * - if none are found, null is returned
     *
     */
    fun getStateByNameContains(stateName: String, ignoreCase: Boolean = false): VeadoState? {

        // Find exact match
        statesAll.forEach {
            it.name?.let { name ->
                if (name == stateName) return it
            }
        }

        //No Exact match Found, search for containing match
        val candidates: ArrayList<VeadoState> = ArrayList()
        statesAll.forEach {
            it.name?.let { name ->
                if (name.contains(stateName, ignoreCase)) {
                    candidates.add(it)
                }
            }
        }

        candidates.let { if (it.isNotEmpty()) it.first() }

        return null
    }

    private val _nodeDataStateIds: MutableList<InstanceStateIdDescription> = mutableListOf()


    /**
     * Get End/Right Side of Node Touch Portal State IDs
     *
     * e.g. name, currentStateId, states.<stateId>.id
     *
     * These should be appended to a base Node ID (not done by this function)
     * e.g. `state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId`
     */
    override fun getNodeDataStateIds(): List<InstanceStateIdDescription> {
        if (_nodeDataStateIds.isEmpty()) {
            // (state.<InstanceTitle>.nodes.<type>.)<nodeId>.id
            _nodeDataStateIds.add(InstanceStateIdDescription(tpStateNodeIdVeadoId, tpStateNodeDescriptionVeadoId))
            _nodeDataStateIds.add(InstanceStateIdDescription(tpStateNodeIdName, tpStateNodeDescriptionName))
            _nodeDataStateIds.add(InstanceStateIdDescription(tpStateNodeIdType, tpStateNodeDescriptionType))

            _nodeDataStateIds.add(
                InstanceStateIdDescription(
                    tpStateNodeIdCurrentStateId, tpStateNodeDescriptionCurrentStateId
                )
            )
            _nodeDataStateIds.add(
                InstanceStateIdDescription(
                    tpStateNodeIdCurrentStateName, tpStateNodeDescriptionCurrentStateName
                )
            )
            _nodeDataStateIds.add(
                InstanceStateIdDescription(
                    tpStateNodeIdCurrentStateThumbnail, tpStateNodeDescriptionCurrentStateThumbnail
                )
            )
            _nodeDataStateIds.add(
                InstanceStateIdDescription(
                    tpStateNodeIdCurrentStateThumbnailHash, tpStateNodeDescriptionCurrentStateThumbnailHash
                )
            )
            _nodeDataStateIds.add(
                InstanceStateIdDescription(
                    tpStateNodeIdCurrentStateThumbnailWidth, tpStateNodeDescriptionCurrentStateThumbnailWidth
                )
            )
            _nodeDataStateIds.add(
                InstanceStateIdDescription(
                    tpStateNodeIdCurrentStateThumbnailHeight, tpStateNodeDescriptionCurrentStateThumbnailHeight
                )
            )

            // For Each State TODO Add option toggle with toggle - (re)generation will need work
            //statesAll.forEach { state -> getTpStateVeadoStateIdDesc(state, listStateIDs) }
        }

        return _nodeDataStateIds.toList()
    }


    fun getTpStateVeadoStateIdDesc(
        state: VeadoState, listStateIDs: MutableList<InstanceStateIdDescription> = mutableListOf()
    ): MutableList<InstanceStateIdDescription> {
        val stateNode = "states.${state.id}"
        listStateIDs.add(InstanceStateIdDescription("$stateNode.id", "State ID - ${state.name ?: state.id}"))
        listStateIDs.add(InstanceStateIdDescription("$stateNode.name", "State Name - ${state.name ?: state.id}"))

        if (state.thumbnail != null) {
            listStateIDs.add(
                InstanceStateIdDescription(
                    "$stateNode$thumbnailPngIdSuffix", "State Thumbnail - ${state.name ?: state.id}"
                )
            )
            listStateIDs.add(
                InstanceStateIdDescription(
                    "$stateNode$thumbnailHashIdSuffix", "State Thumbnail Hash - ${state.name ?: state.id}"
                )
            )
            listStateIDs.add(
                InstanceStateIdDescription(
                    "$stateNode$thumbnailWidthIdSuffix", "State Thumbnail Width - ${state.name ?: state.id}"
                )
            )
            listStateIDs.add(
                InstanceStateIdDescription(
                    "$stateNode$thumbnailHeightIdSuffix", "State Thumbnail Height - ${state.name ?: state.id}"
                )
            )
        }
        return listStateIDs
    }


    /**
     * Get End/Right Side of Node Touch Portal State IDs Mapped vs their values
     *
     * e.g. name, currentStateId, states.<stateId>.id
     *
     * Map has data inserted in the following order:
     * - node id, name, type
     * - Current State values (currentStateId, currentStateName, currentStateThumbnailPng, etc.)
     * - All state values
     *
     * State values are inserted in the following order:
     * id - name - Png - Hash - Height
     *
     * These should be appended to a base Node ID (not done by this function)
     * e.g. `state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId`
     */
    override fun getNodeDataValues(): Map<String, String> {
        return allNodeDataValues
    }

    override fun getNodeTPStateIDLabelMap(): Map<String, String> {
        return StateNodeTPStateIDLabelMap
    }

    private val _allNodeDataValues = mutableMapOf<String, String>()

    val allNodeDataValues: Map<String, String>
        get() {
            if (!stateIdNodeValuesClean) {
                generateNodeDataValues()
            }
            return _allNodeDataValues.toMap()
        }


    private var stateIdNodeValuesClean = false;


    /**
     * Get End/Right Side of Node Touch Portal State IDs Mapped vs their values
     *
     * e.g. name, currentStateId, states.<stateId>.id
     *
     * Map has data inserted in the following order:
     * - node id, name, type
     * - Current State values (currentStateId, currentStateName, currentStateThumbnailPng, etc.)
     * - All state values
     *
     * State values are inserted in the following order:
     * id - name - Png - Hash - Height
     *
     * These should be appended to a base Node ID (not done by this function)
     * e.g. `state.<InstanceTitle>.nodes.<type>.<nodeId>.currentStateId`
     */
    fun generateNodeDataValues() {
        _allNodeDataValues.clear()

        // (state.<InstanceTitle/Number>.nodes.<type>.<nodeId>.)id
        _allNodeDataValues[tpStateNodeIdVeadoId] = id
        // (state.<InstanceTitle/Number>.nodes.<type>.<nodeId>.)name
        _allNodeDataValues[tpStateNodeIdName] = name
        // (state.<InstanceTitle/Number>.nodes.<type>.<nodeId>.)type
        _allNodeDataValues[tpStateNodeIdType] = type

        when (val currState = currentState) {
            null -> {
                _allNodeDataValues["currentStateId"] = ""
                _allNodeDataValues["currentStateName"] = ""
                _allNodeDataValues["currentStateThumbnailPng"] = ""
                _allNodeDataValues["currentStateThumbnailHash"] = ""
                _allNodeDataValues["currentStateThumbnailWidth"] = ""
                _allNodeDataValues["currentStateThumbnailHeight"] = ""
            }

            else -> {
                _allNodeDataValues["currentStateId"] = currState.id
                _allNodeDataValues["currentStateName"] = currState.name ?: ""

                val thumb = currState.thumbnail
                if (thumb == null) {
                    _allNodeDataValues["currentStateThumbnailPng"] = ""
                    _allNodeDataValues["currentStateThumbnailHash"] = ""
                    _allNodeDataValues["currentStateThumbnailWidth"] = ""
                    _allNodeDataValues["currentStateThumbnailHeight"] = ""
                } else {
                    _allNodeDataValues["currentStateThumbnailPng"] = thumb.png
                    _allNodeDataValues["currentStateThumbnailHash"] = thumb.hash
                    _allNodeDataValues["currentStateThumbnailWidth"] = thumb.widthOrBlank
                    _allNodeDataValues["currentStateThumbnailHeight"] = thumb.heightOrBlank
                }
            }
        }

        // For Each State TODO Add option toggle with toggle
        //statesAll.forEach { state -> getTpStateVeadoStateValues(state, stateIdNodeValues) }

        stateIdNodeValuesClean = true;

    }

    override var name: String
        get() = super.name
        set(value) {
            super.name = value
            _allNodeDataValues[tpStateNodeIdName] = value
        }


    inline fun inlineGetPropertyTpStateIdValue(postProcessActions: (id: String, value: String?) -> Unit) {
        val currentState = currentState

        //postProcessActions(tpStateNodeIdVeadoId, id)
        //postProcessActions(tpStateNodeIdType, type)
        postProcessActions(tpStateNodeIdName, name)

        when (currentState) {
            null -> {
                postProcessActions(tpStateNodeIdCurrentStateId, null)
                postProcessActions(tpStateNodeIdCurrentStateName, null)
                postProcessActions(tpStateNodeIdCurrentStateThumbnailHash, null)
                postProcessActions(tpStateNodeIdCurrentStateThumbnail, null)
                postProcessActions(tpStateNodeIdCurrentStateThumbnailWidth, null)
                postProcessActions(tpStateNodeIdCurrentStateThumbnailHeight, null)
            }

            else -> {
                postProcessActions(tpStateNodeIdCurrentStateId, currentState.id)
                postProcessActions(tpStateNodeIdCurrentStateName, currentState.name)

                val thumb = currentState.thumbnail
                if (thumb == null) {
                    postProcessActions(tpStateNodeIdCurrentStateThumbnailHash, null)
                    postProcessActions(tpStateNodeIdCurrentStateThumbnail, null)
                    postProcessActions(tpStateNodeIdCurrentStateThumbnailWidth, null)
                    postProcessActions(tpStateNodeIdCurrentStateThumbnailHeight, null)
                } else {
                    postProcessActions(tpStateNodeIdCurrentStateThumbnailHash, thumb.hash)
                    postProcessActions(tpStateNodeIdCurrentStateThumbnail, thumb.png)
                    postProcessActions(
                        tpStateNodeIdCurrentStateThumbnailWidth,
                        thumb.widthOrBlank
                    )
                    postProcessActions(
                        tpStateNodeIdCurrentStateThumbnailHeight,
                        thumb.heightOrBlank
                    )
                }
            }
        }
    }

    inline fun inlineGetPropertyTpStateIdLabel(postProcessActions: (id: String, label: String) -> Unit) {
        //postProcessActions(tpStateNodeIdVeadoId, tpStateNodeDescriptionVeadoId)
        //postProcessActions(tpStateNodeIdType, tpStateNodeDescriptionType)
        postProcessActions(tpStateNodeIdName, tpStateNodeDescriptionName)

        postProcessActions(tpStateNodeIdCurrentStateId, tpStateNodeDescriptionCurrentStateId)
        postProcessActions(tpStateNodeIdCurrentStateName, tpStateNodeDescriptionCurrentStateName)

        postProcessActions(tpStateNodeIdCurrentStateThumbnail, tpStateNodeDescriptionCurrentStateThumbnail)
        postProcessActions(tpStateNodeIdCurrentStateThumbnailHash, tpStateNodeDescriptionCurrentStateThumbnailHash)
        postProcessActions(tpStateNodeIdCurrentStateThumbnailWidth, tpStateNodeDescriptionCurrentStateThumbnailWidth)
        postProcessActions(tpStateNodeIdCurrentStateThumbnailHeight, tpStateNodeDescriptionCurrentStateThumbnailHeight)
    }


    inline fun inlineGetPropertyTpStateIdLabelValue(postProcessActions: (id: String, label: String, value: String?) -> Unit) {
        val currentState = currentState

        //postProcessActions(tpStateNodeIdVeadoId, tpStateNodeDescriptionVeadoId, id)
        //postProcessActions(tpStateNodeIdType, tpStateNodeDescriptionType, type)
        postProcessActions(tpStateNodeIdName, tpStateNodeDescriptionName, name)

        if (currentState == null) {
            postProcessActions(
                tpStateNodeIdCurrentStateId, tpStateNodeDescriptionCurrentStateId, null
            )
            postProcessActions(
                tpStateNodeIdCurrentStateName, tpStateNodeDescriptionCurrentStateName, null
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnail, tpStateNodeDescriptionCurrentStateThumbnail, null
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailHash, tpStateNodeDescriptionCurrentStateThumbnailHash, null
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailWidth, tpStateNodeDescriptionCurrentStateThumbnailWidth, null
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailHeight, tpStateNodeDescriptionCurrentStateThumbnailHeight, null
            )
            return
        }


        postProcessActions(
            tpStateNodeIdCurrentStateId, tpStateNodeDescriptionCurrentStateId, currentState.id
        )
        postProcessActions(
            tpStateNodeIdCurrentStateName, tpStateNodeDescriptionCurrentStateName, currentState.name
        )

        val thumb = currentState.thumbnail
        if (thumb == null) {
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnail, tpStateNodeDescriptionCurrentStateThumbnail, null
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailHash, tpStateNodeDescriptionCurrentStateThumbnailHash, null
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailWidth, tpStateNodeDescriptionCurrentStateThumbnailWidth, null
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailHeight,
                tpStateNodeDescriptionCurrentStateThumbnailHeight,
                null
            )

        } else {
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnail, tpStateNodeDescriptionCurrentStateThumbnail,
                thumb.png
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailHash, tpStateNodeDescriptionCurrentStateThumbnailHash,
                thumb.hash
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailWidth, tpStateNodeDescriptionCurrentStateThumbnailWidth,
                thumb.widthOrBlank
            )
            postProcessActions(
                tpStateNodeIdCurrentStateThumbnailHeight, tpStateNodeDescriptionCurrentStateThumbnailHeight,
                thumb.heightOrBlank
            )

        }

    }

    override fun getNodePropertyIdLabels(map: MutableMap<String, String>): Map<String, String> {
        inlineGetPropertyTpStateIdLabel { id, label -> map[id] = label }
        return map
    }

    override fun getNodePropertyIdValues(map: MutableMap<String, String>): Map<String, String> {
        inlineGetPropertyTpStateIdValue { id, value -> map[id] = value ?: "" }
        return map
    }


    fun getTpStateVeadoStateValues(state: VeadoState, stateIdNodeValues: MutableMap<String, String> = mutableMapOf()) {
        // (state.<InstanceTitle>.nodes.<type>.<nodeId>.)states.<stateId> (No trailing .)
        val stateNode = "states.${state.id}"
        stateIdNodeValues["$stateNode.id"] = state.id
        stateIdNodeValues["$stateNode.name"] = state.name ?: ""

        val thumb = state.thumbnail
        if (thumb != null) {
            stateIdNodeValues["$stateNode.thumbnailPng"] = thumb.png
            stateIdNodeValues["$stateNode.thumbnailHash"] = thumb.hash
            stateIdNodeValues["$stateNode.thumbnailWidth"] = thumb.widthOrBlank
            stateIdNodeValues["$stateNode.thumbnailHeight"] = thumb.heightOrBlank
        }
    }

}