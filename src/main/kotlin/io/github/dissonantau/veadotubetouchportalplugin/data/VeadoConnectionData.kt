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
class VeadoConnectionData(connection: Connection) {

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
     * ArrayList of all [VtState] available
     *
     * In order received from API, so they should be in the order they appear in Veadotube
     *
     * IDs are unique but Names can be duplicates.
     *
     * Work with this should probably be synchronised using the connection obj
     */
    var statesAll =
        ArrayList<VtState>()
        private set

    /**
     * Map of States - `ID` vs `VtState`
     *
     * Work with this should probably be synchronised using the connection obj
     */
    private val statesByID = HashMap<String, VtState>()

    /**
     * Get [VtState] from this connection by State ID
     *
     * ID = Name from Mini 2.1
     */
    fun getStateByID(stateID: String): VtState? = statesByID[stateID]

    /**
     * Get [VtState] from this connection by State Name
     *
     * - if an exact match is found, it is chosen
     * - If no exact match is found, the first result that that contains the give name is returned (ignoreCase is used here)
     * - if none are found, null is returned
     *
     */
    fun getStateByNameContains(stateName: String, ignoreCase: Boolean = false): VtState? {
        connection?.run {

            // Find exact match
            statesAll.forEach {
                it.name?.let { name ->
                    if (name == stateName) return it
                }
            }

            //No Exact match Found, search for containing match
            val candidates: ArrayList<VtState> = ArrayList()
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
    var currentState: VtState? = null
        private set

    /**
     * Current State Thumbnail
     *
     * Prevents deletion if several thumbnails are fetched and pushes the current state out of the LRU Maps
     *
     */
    private var currentStateThumbnail: VtThumbnail? = null

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
    private val thumbnailHardLRUMap: LRUMap<VtState, VtThumbnail> = LRUMap<VtState, VtThumbnail>(lruHardMapSize)

    /**
     * Least Recently Used Map with Soft References
     *
     * Will keep #[lruSoftMapSize] last used values to reduce chance of deletion by GC
     *
     * See: [Apache Commons LRUMap](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/map/LRUMap.html)
     */
    private val thumbnailSoftLRUMap: LRUMap<VtState, SoftReference<VtThumbnail>> =
        LRUMap<VtState, SoftReference<VtThumbnail>>(lruSoftMapSize)


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
     * Generated State ID for mini Current Avatar Name
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstance.myTitle.state.currentAvatarStateName
     * */
    var stateIDTitledCurrentAvatarName = ""
        private set

    var stateIDTitledCurrentAvatarNameShort = ""
        private set

    /**
     * Generated State ID for mini Current Avatar Name
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstance.myTitle.state.currentAvatarStateThumbnail
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
    fun refreshInstanceTitle(): List<String> {
        val firstDash = instance.title.indexOf('-')
        instanceTitleCleaned = if (firstDash >= 0) {
            instance.title.substring(firstDash + 1).trim()
        } else {
            "${instance.id.type}-${instance.id.process}"
        }

        val list: MutableList<String> = mutableListOf()

        instanceTitleSimplified = instanceTitleCleaned.filter { it.isLetterOrDigit() }

        list.add(stateIDTitledCurrentAvatarNameShort)
        stateIDTitledCurrentAvatarName =
            "${VeadoTouchPluginConstants.ID}.MiniInstance.state.$instanceTitleSimplified.currentAvatarStateName"

        stateIDTitledCurrentAvatarNameShort = "$instanceTitleSimplified.currentAvatarStateName"


        list.add(stateIDTitledCurrentAvatarThumbnailShort)
        stateIDTitledCurrentAvatarThumbnail =
            "${VeadoTouchPluginConstants.ID}.MiniInstance.state.$instanceTitleSimplified.currentAvatarStateThumbnail"

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
     * Generated State ID for mini Current Avatar Name
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstance.state.1.currentAvatarStateName
     * */
    var stateIDNumberedCurrentAvatarName = ""
        private set

    var stateIDNumberedCurrentAvatarNameShort = ""
        private set

    /**
     * Generated State ID for mini Current Avatar Name
     * e.g. io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.MiniInstance.state.1.currentAvatarStateThumbnail
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
    fun refreshInstanceNumber(): List<String> {
        val list: MutableList<String> = mutableListOf()

        list.add(stateIDNumberedCurrentAvatarNameShort)
        stateIDNumberedCurrentAvatarName =
            "${VeadoTouchPluginConstants.ID}.MiniInstance.state.$instanceNumber.currentAvatarStateName"

        stateIDNumberedCurrentAvatarNameShort = "$instanceNumber.currentAvatarStateName"


        list.add(stateIDNumberedCurrentAvatarThumbnailShort)
        stateIDNumberedCurrentAvatarThumbnail =
            "${VeadoTouchPluginConstants.ID}.MiniInstance.state.$instanceNumber.currentAvatarStateThumbnail"

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
        val newAllList: ArrayList<VtState> = ArrayList(stateList.size)

        // Track if list has changed - Count, Names, etc. - We want to regenerate the list and send to Touch Portal
        var listReplace = false
        // If Count is different, mark as changed
        if (statesAll.count() != stateList.count()) listReplace = true

        for (i in stateList.indices) {

            val stateListNew = stateList[i]
            val stateListOld = statesAll.getOrNull(i)

            LOGGER.trace { "updateStates: stateList item ${i}; new ${stateListNew.id},${stateListNew.name}; old ${stateListOld?.id},${stateListOld?.name}" }

            val existingState: VtState =
                if (stateListOld != null && stateListNew.id == stateListOld.id) {
                    //Order Match, get state from array
                    LOGGER.trace { "updateStates: stateList item ${i}; new ${stateListNew.id} == old ${stateListOld.id}" }

                    stateListOld
                } else {
                    //Not the same, we need to check if it exists in HashMap and get, or create new one from new State
                    LOGGER.trace { "updateStates: stateList item ${i}; new ${stateListNew.id} != old ${stateListOld?.id}" }
                    LOGGER.trace { "updateStates: stateList item ${i}; statesByID[stateListNew.id] == old ${statesByID[stateListNew.id]?.id},${statesByID[stateListNew.id]?.name}}" }
                    listReplace = true
                    LOGGER.trace { "updateStates: New VtState > listReplace = $listReplace" }

                    statesByID.getOrPut(stateListNew.id) {
                        LOGGER.trace { "updateStates: create VtState for ${stateListNew.id}" }
                        VtState(stateListNew).also {
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
            val newCurrentState: VtState = statesByID.getOrPut(stateID.state) { VtState(stateID) }

            // Update and return
            currentState = newCurrentState
            return true

        }

    }


    /** Clear currentStateThumbnail, Maps, etc. */
    fun clearedStateThumbnail(vtState: VtState) {
        vtState.clearThumbnail()

        if (currentState == vtState) {
            currentStateThumbnail = null
        }

        thumbnailHardLRUMap.remove(vtState)
        thumbnailSoftLRUMap.remove(vtState)
    }

    /** Updates a State Thumbnail, creating the state if it doesn't exist
     *
     * @param payload Payload with State Thumbnail Data to update
     * @param updateToMRU forces the State Thumbnail to be made the *Most Recently Used* on in the Soft and Hard Thumbnail Maps
     */
    fun updateStateThumbnail(payload: BleatkanStateThumbnail, updateToMRU: Boolean = false): Boolean {
        //Get State, create if it doesn't exist (Thumbnail Payload is used, but Thumbnail info not used so se get accurate update response)
        val newCurrentState: VtState = statesByID.getOrPut(payload.state) { VtState(payload) }


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

/**
 * Object to store collected Avatar State info
 */
@Suppress("unused")
class VtState
@JvmOverloads constructor(
    /** State ID */
    val id: String,
    /** State Name */
    name: String? = null
) {
    //Construct from a State
    constructor(state: State) : this(
        id = state.id,
        name = state.name
    )

    //Construct from Peek - Use if Peek is received and State doesn't exist
    constructor(state: BleatkanStatePeek) : this(
        id = state.state
    )

    /**
     * Construct from Thumbnail Payload
     *
     * Used if Thumbnail is received and State doesn't exist.
     *
     * DOES NOT ADD THUMBNAIL Data, call [updateThumbnail] after creation
     *
     * [updateThumbnail] returns Boolean for if PNG was updated, which can't be returned from a constructor
     */
    constructor(payload: BleatkanStateThumbnail) : this(
        id = payload.state
    )

    /** State Name
     *
     * Null if not set
     */
    var name: String? = name
        internal set

    /**
     * Updates stored name from State if ID matches and name isn't blank
     * @return true if [name] was updated
     */
    internal fun update(state: State): Boolean {
        if (state.id == id && name != state.name && state.name.isNotBlank()) {
            name = state.name
            return true
        }
        return false
    }

    /**
     * Updates stored PNG from State
     *
     * @return true if [thumbnail] was updated
     */
    internal fun update(state: BleatkanStateThumbnail): Boolean = updateThumbnail(state)

    /**
     * Updates stored PNG
     *
     * @return true if [thumbnail] was updated
     */
    internal fun updateThumbnail(newThumbnail: BleatkanStateThumbnail): Boolean {
        if (newThumbnail.state != id) return false

        // Get Existing thumbnail obj, create new if null
        when (val thumb = _thumbnail?.get()) {
            null -> VtThumbnail(newThumbnail).also {
                //No Existing thumbnail
                this._thumbnail = WeakReference(it)
                return true
            }

            else -> {
                // Update Existing Thumbnail
                return thumb.updateThumbnail(newThumbnail)
            }
        }

    }

    /**
     * Clears any stored Thumbnails
     *
     * @return True if cleared - false means it was already null. Will still return true if thumbnail was weakly stored but was cleared before this was called
     */
    internal fun clearThumbnail(): Boolean {
        if (_thumbnail != null) {
            _thumbnail = null

            return true
        }
        return false
    }

    /**
     * State Thumbnail backing var
     *
     * Weak Ref to allow cleanup if removed from Cache and Cleaned up
     */
    private var _thumbnail: WeakReference<VtThumbnail>? = null

    /**
     * State Thumbnail
     *
     * Base64 PNG
     *
     * May be `null` even if previously fetched and removed from Cache and cleaned up by GC
     *
     */
    val thumbnail: VtThumbnail?
        get() = _thumbnail?.get()

}

/** Represents a Veadotube Thumbnail */
@Suppress("MemberVisibilityCanBePrivate")
class VtThumbnail(thumbnail: BleatkanStateThumbnail) {

    /** State Thumbnail - as PNG */
    var png: String = ""
        private set
    var width: Int = -1
        private set
    var height: Int = -1
        private set

    /**
     * Hash of Thumbnail from Veadotube
     *
     * Value introduced in 2.1, 2.0 doesn't provide this
     *
     * Blank if not set or not provided (pre 2.1)
     */
    var hash: String = ""
        private set

    /**
     * Hash of Thumbnail before any transformations (Resize, etc.)
     *
     * 1 if not set or Hash is provided by Veado (2.1 and later)
     *
     */
    var pngHash: Int = -1
        private set

    init {
        val veadoHash = thumbnail.hash
        if (veadoHash != null) {
            hash = veadoHash

            //Update PNG
            this.width = thumbnail.width
            this.height = thumbnail.height
            this.png = thumbnail.png
        } else {
            pngHash = thumbnail.png.hashCode()

            //Update PNG
            this.width = thumbnail.width
            this.height = thumbnail.height
            this.png = thumbnail.png
        }
    }


    /**
     * Updates stored PNG
     *
     * @return true if [png] was updated
     */
    internal fun updateThumbnail(thumbnail: BleatkanStateThumbnail): Boolean {
        val veadoHash = thumbnail.hash
        if (veadoHash != null) {
            // 2.1 and later: compare Hash from Veado then update if different
            if (hash != veadoHash) {
                hash = veadoHash

                //Update PNG
                this.width = thumbnail.width
                this.height = thumbnail.height
                this.png = thumbnail.png

                return true
            }
        } else {
            // Pre-2.1 - Generate and use String HashCode then update if different
            if (pngHash != thumbnail.png.hashCode()) {
                pngHash = thumbnail.png.hashCode()

                //Update PNG
                this.width = thumbnail.width
                this.height = thumbnail.height
                this.png = thumbnail.png

                return true
            }
        }

        return false
    }

}