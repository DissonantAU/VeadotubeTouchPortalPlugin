package io.github.dissonantau.veadotubetouchportalplugin.data

import org.apache.commons.collections4.map.LRUMap
import io.github.dissonantau.bleatkan.connection.Connection
import io.github.dissonantau.bleatkan.message.State
import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPluginConstants
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadState as BleatkanStatePeek
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadStateList as BleatkanStateList
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadPng as BleatkanStateThumbnail

@Suppress("unused")
class VeadoStateNodeData(connection: Connection) {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    /** Connection Weak Ref - used to prevent GC issues */
    private val _connection: WeakReference<Connection> = WeakReference(connection)

    /** [Connection] this data belongs to.
     *
     *  Backed by a [WeakReference] - returns null if Connection has been dereferenced elsewhere and cleaned
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val connection: Connection?
        get() {
            return _connection.get()
        }

    /** Instance this data belongs to */
    private val instance = connection.instance

    /**
     * ArrayList of all [VeadoState] available
     *
     * In order received from API, so they should be in the order they appear in Veadotube
     *
     * IDs are unique but Names can be duplicates.
     *
     * Work with this should probably be synchronised using the connection obj
     */
    var statesAll =
        ArrayList<VeadoState>()
        private set

    /**
     * Map of States - `ID` vs `VeadoState`
     *
     * Work with this should probably be synchronised using the connection obj
     */
    private val statesByID = HashMap<String, VeadoState>()

    /**
     * Get [VeadoState] from this connection by State ID
     *
     * ID = Name from Mini 2.1
     */
    fun getStateByID(stateID: String): VeadoState? = statesByID[stateID]

    /**
     * Get [VeadoState] from this connection by State Name
     *
     * - if an exact match is found, it is chosen
     * - If no exact match is found, the first result that that contains the give name is returned (ignoreCase is used here)
     * - if none are found, null is returned
     *
     */
    fun getStateByNameContains(stateName: String, ignoreCase: Boolean = false): VeadoState? {
        connection?.run {

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
        }

        return null
    }

    /**
     * Pair with the current Avatar State with `ID` and `Name`
     *
     * Names can be duplicates, so the Current state may not match a value in the collectionStates
     *
     * If there's no match, it should request a new list in case a new value was added since list was fetched
     */
    var currentState: VeadoState? = null
        private set

    /**
     * Current State Thumbnail
     *
     * Prevents deletion if several thumbnails are fetched and pushes the current state out of the LRU Maps
     *
     */
    private var currentStateThumbnail: VeadoThumbnail? = null

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
    private val thumbnailHardLRUMap: LRUMap<VeadoState, VeadoThumbnail> = LRUMap<VeadoState, VeadoThumbnail>(lruHardMapSize)

    /**
     * Least Recently Used Map with Soft References
     *
     * Will keep #[lruSoftMapSize] last used values to reduce chance of deletion by GC
     *
     * See: [Apache Commons LRUMap](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/LRUMap.html)
     */
    private val thumbnailSoftLRUMap: LRUMap<VeadoState, SoftReference<VeadoThumbnail>> =
        LRUMap<VeadoState, SoftReference<VeadoThumbnail>>(lruSoftMapSize)


    // StateID Strings for Mini Instances
    /** Instance Title
     *
     * Simplified Title after first Dash, if one is set
     * * only Letters and Digits are kept - no spaces, etc. ( e.g. 'veadotube mini - my @ title 3!' becomes 'mytitle3')
     *
     * If no non-default title exists (e.g. 'veadotube mini') the Type & Process ID are used (mini123456)
     *
     * This should be updated if title changes
     * */
    var instanceTitleSimplified: String = ""
        private set

    /** Instance Title
     *
     * Cleaned Title after first Dash, if one is set
     * * Trimmed ( e.g. 'veadotube mini - my @ title 3!' becomes 'my @ title 3!')
     *
     * If no non-default title exists (e.g. 'veadotube mini') the Type & Process ID are used (mini-123456)
     *
     * This should be updated if title changes
     * */
    var instanceTitleCleaned: String = ""
        private set

    /**
     * Generated State ID for mini Windows Title
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.myTitle.state.currentAvatarStateName
     * */
    var stateIDTitledInstanceTitle = ""
        private set

    var stateIDTitledInstanceTitleShort = ""
        private set

    /**
     * Generated State ID for mini Current Avatar Name
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.myTitle.state.currentAvatarStateName
     * */
    var stateIDTitledCurrentAvatarName = ""
        private set

    var stateIDTitledCurrentAvatarNameShort = ""
        private set

    /**
     * Generated State ID for mini Current Avatar Name
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.myTitle.state.currentAvatarStateThumbnail
     * */
    var stateIDTitledCurrentAvatarThumbnail = ""
        private set

    var stateIDTitledCurrentAvatarThumbnailShort = ""
        private set

    init {
        refreshInstanceTitle()
    }

    /**
     * Updates [instanceTitleSimplified]
     *
     * Returns old State IDs for Removal
     */
    fun refreshInstanceTitle(): MutableList<String> {
        val firstDash = instance.title.indexOf('-')
        instanceTitleCleaned = if (firstDash >= 0) {
            instance.title.substring(firstDash + 1).trim()
        } else {
            "${instance.id.type}-${instance.id.process}"
        }

        val list: MutableList<String> = mutableListOf()

        instanceTitleSimplified = instanceTitleCleaned.filter { it.isLetterOrDigit() }

        list.add(stateIDTitledInstanceTitleShort)
        stateIDTitledInstanceTitle =
            "${VeadoTouchPluginConstants.ID}.MiniInstances.state.$instanceTitleSimplified.title"

        stateIDTitledInstanceTitleShort =
            "$instanceTitleSimplified.title"


        list.add(stateIDTitledCurrentAvatarNameShort)
        stateIDTitledCurrentAvatarName =
            "${VeadoTouchPluginConstants.ID}.MiniInstances.state.$instanceTitleSimplified.currentAvatarStateName"

        stateIDTitledCurrentAvatarNameShort =
            "$instanceTitleSimplified.currentAvatarStateName"



        list.add(stateIDTitledCurrentAvatarThumbnailShort)
        stateIDTitledCurrentAvatarThumbnail =
            "${VeadoTouchPluginConstants.ID}.MiniInstances.state.$instanceTitleSimplified.currentAvatarStateThumbnail"

        stateIDTitledCurrentAvatarThumbnailShort =
            "$instanceTitleSimplified.currentAvatarStateThumbnail"

        return list
    }


    /**
     * Number for Mapping Instance in Touch Portal
     *
     * Related to Age (1st 2nd, etc.)
     */
    private var instanceNumber = -1

    /**
     * Generated State ID for mini Window Title
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.1.currentAvatarStateName
     * */
    var stateIDNumberedInstanceTitle = ""
        private set

    var stateIDNumberedInstanceTitleShort = ""
        private set


    /**
     * Generated State ID for mini Current Avatar Name
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.1.currentAvatarStateName
     * */
    var stateIDNumberedCurrentAvatarName = ""
        private set

    var stateIDNumberedCurrentAvatarNameShort = ""
        private set

    /**
     * Generated State ID for mini Current Avatar Name
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstances.state.1.currentAvatarStateThumbnail
     * */
    var stateIDNumberedCurrentAvatarThumbnail = ""
        private set

    var stateIDNumberedCurrentAvatarThumbnailShort = ""
        private set

    fun getInstanceNumber(): Int {
        return instanceNumber
    }

    fun setInstanceNumber(newInstanceNumber: Int): Int {
        val oldInstanceNumber = instanceNumber
        instanceNumber = newInstanceNumber
        return oldInstanceNumber
    }

    /**
     * Updates [instanceTitleSimplified]
     *
     * Returns old State IDs for Removal
     */
    fun refreshInstanceNumber(): MutableList<String> {
        val list: MutableList<String> = mutableListOf()

        list.add(stateIDNumberedInstanceTitleShort)
        stateIDNumberedInstanceTitle =
            "${VeadoTouchPluginConstants.ID}.MiniInstances.state.$instanceNumber.title"

        stateIDNumberedInstanceTitleShort =
            "$instanceNumber.title"


        list.add(stateIDNumberedCurrentAvatarNameShort)
        stateIDNumberedCurrentAvatarName =
            "${VeadoTouchPluginConstants.ID}.MiniInstances.state.$instanceNumber.currentAvatarStateName"

        stateIDNumberedCurrentAvatarNameShort =
            "$instanceNumber.currentAvatarStateName"


        list.add(stateIDNumberedCurrentAvatarThumbnailShort)
        stateIDNumberedCurrentAvatarThumbnail =
            "${VeadoTouchPluginConstants.ID}.MiniInstances.state.$instanceNumber.currentAvatarStateThumbnail"

        stateIDNumberedCurrentAvatarThumbnailShort =
            "$instanceNumber.currentAvatarStateThumbnail"

        return list
    }

    /**
     * Replaces all states
     *
     * If the list's don't match, items are checked and updated
     */
    fun updateStates(payload: BleatkanStateList) {
        LOGGER.trace { "updateStates: Begin" }

        val stateList = payload.states
        // New List, lazy initialised to create on unless needed
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
        }

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
            //Already matches
            return false

        } else {
            // Not a match
            val newCurrentState: VeadoState = statesByID.getOrPut(stateID.state) { VeadoState(stateID) }

            // Update and return
            currentState = newCurrentState
            return true

        }

    }


    /** Clear currentStateThumbnail, Maps, etc. */
    fun clearedStateThumbnail(veadoState: VeadoState) {
        veadoState.clearThumbnail()

        if (currentState == veadoState) {
            currentStateThumbnail = null
        }

        thumbnailHardLRUMap.remove(veadoState)
        thumbnailSoftLRUMap.remove(veadoState)
    }

    /** Updates a State Thumbnail, creating the state if it doesn't exist
     *
     * @param payload Payload with State Thumbnail Data to update
     * @param updateToMRU forces the State Thumbnail to be made the *Most Recently Used* on in the Soft and Hard Thumbnail Maps
     */
    fun updateStateThumbnail(payload: BleatkanStateThumbnail, updateToMRU: Boolean = false): Boolean {
        //Get State, create if it doesn't exist (Thumbnail Payload is used, but Thumbnail info not used so se get accurate update response)
        val newCurrentState: VeadoState = statesByID.getOrPut(payload.state) { VeadoState(payload) }


        // Update and return Success Boolean
        return newCurrentState.updateThumbnail(payload).also {
            if (it) {
                newCurrentState.thumbnail?.let { newThumbnail ->

                    if (currentState == newCurrentState) {
                        currentStateThumbnail = newThumbnail
                    }

                    //If in current Hard Map, object should have updated already
                    if ((thumbnailHardLRUMap.get(
                            newCurrentState,
                            updateToMRU
                        ) == null && updateToMRU) || !thumbnailHardLRUMap.isFull
                    ) {
                        //If NOT in list, and update to MRU is true OR Map isn't full - Add
                        thumbnailHardLRUMap[newCurrentState] = newThumbnail
                    }

                    //If in current Soft Map, object should have updated already
                    if ((thumbnailSoftLRUMap.get(
                            newCurrentState,
                            updateToMRU
                        )?.get() == null && updateToMRU) || !thumbnailSoftLRUMap.isFull
                    ) {
                        //If NOT in list, and update to MRU is true OR Map isn't full - Add
                        thumbnailSoftLRUMap[newCurrentState] = SoftReference(newThumbnail)
                    }

                }
            }
        }
    }

}