package io.github.dissonantau.veadotubetouchportalplugin.data

import org.apache.commons.collections4.map.LRUMap
import io.github.dissonantau.bleatkan.connection.Connection
import io.github.dissonantau.bleatkan.message.State
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadState as BleatkanStatePeek
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadStateList as BleatkanStateList
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadPng as BleatkanStateThumbnail

@Suppress("unused")
class VeadoConnectionData(connection: Connection) {

    /** Connection Weak Ref - used to prevent GC issues */
    private val _connection: WeakReference<Connection> = WeakReference(connection)

    /** [Connection] this data belongs to.
     *
     *  Backed by a [WeakReference] - returns null if Connection has been dereferenced elsewhere and cleaned
     */
    val connection: Connection?
        get() {
            return _connection.get()
        }

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

    /** Get [VtState] from this connection by State ID*/
    fun getStateByID(stateID: String): VtState? = statesByID[stateID]

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
    private val lruHardMapSize = 5

    /** Default Size of LRU Map - Soft References*/
    private val lruSoftMapSize = lruHardMapSize + 5

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


    /** Replaces all states
     *
     * If the list's don't match, items are checked and updated
     *
     */
    fun updateStates(payload: BleatkanStateList) {

        val stateList = payload.states
        // New List, lazy initialised to create on unless needed
        val newAllList: ArrayList<VtState> = ArrayList(stateList.size)
        // First ID where we started copying - < 0 means we didn't need to
        var listReplace = false


        for (i in stateList.indices) {

            val stateListNew = stateList[i]
            val stateListOld = statesAll.getOrNull(i)

            val existingState: VtState =
                if (stateListNew.id == stateListOld?.id) {
                    //Order Match, get state from array
                    stateListOld.also {
                        it.update(stateListNew)
                    }
                } else {
                    //Not the same, we need to check if it exists in HashMap and get, or create new one from new State
                    statesByID.getOrPut(stateListNew.id) { listReplace = true; VtState(stateListNew) }
                }

            //Add to new List if we need to
            newAllList.add(existingState)

        }


        //If List changed (order, new items, etc.)
        if (listReplace) {
            // Replace Old List
            statesAll = newAllList

            //Update Maps - converts statesAll to HashSet for performance
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
        if (state.id == id && state.name.isNotBlank()) {
            name = state.name
            return true
        }
        return false
    }

    /**
     * Updates stored PNG from State if ID matches and PNG isn't blank
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

        if (newThumbnail.png.isNotBlank() &&
            _thumbnail?.get()?.pngHash != newThumbnail.png.hashCode()
        ) {
            // Get Existing thumbnail obj, create new if null
            when (val thumb = _thumbnail?.get()) {
                null -> VtThumbnail(newThumbnail).also {
                    //No Existing thumbnail
                    this._thumbnail = WeakReference(it)
                    return true
                }

                else -> return thumb.updateThumbnail(newThumbnail) // Update Existing Thumbnail
            }


        }
        return false
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
    lateinit var png: String
        private set
    var width: Int = -1
        private set
    var height: Int = -1
        private set

    /**
     * Hash of Thumbnail before any transformations (Resize, etc.)
     *
     * Returns -1 if thumbnail is not set
     */
    var pngHash: Int = -1
        private set

    init {
        //Init Thumbnail incl png - will always set PNG
        updateThumbnail(thumbnail)
    }


    /**
     * Updates stored PNG
     *
     * @return true if [png] was updated
     */
    internal fun updateThumbnail(thumbnail: BleatkanStateThumbnail): Boolean {
        if (!this::png.isInitialized ||
            thumbnail.png.isNotBlank() &&
            pngHash != thumbnail.png.hashCode()
        ) {
            // May require processing to make square for Touch Portal, so store the original hash
            pngHash = thumbnail.png.hashCode()

            //Update PNG
            this.width = thumbnail.width
            this.height = thumbnail.height
            this.png = thumbnail.png

            return true
        }
        return false
    }


}