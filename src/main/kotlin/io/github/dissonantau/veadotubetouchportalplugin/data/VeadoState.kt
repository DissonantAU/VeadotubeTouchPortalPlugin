package io.github.dissonantau.veadotubetouchportalplugin.data

import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadPng
import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadState
import io.github.dissonantau.bleatkan.message.State
import java.lang.ref.WeakReference

/**
 * Object to store collected Avatar State info
 */
@Suppress("unused")
class VeadoState
@JvmOverloads constructor(
    /** State ID */
    val id: String,
    /** State Name */
    name: String? = null,
    /** Thumbnail Hash */
    thumbHash: String? = null
) {
    // Construct from a State
    constructor(state: State) : this(
        id = state.id,
        name = state.name,
        thumbHash = state.thumbHash
    )

    // Construct from Peek - Use if Peek is received and State doesn't exist.
    // Name should be checked and list requested at some point.
    constructor(state: ResultPayloadState) : this(
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
    constructor(payload: ResultPayloadPng) : this(
        id = payload.state
    )

    /** State Name
     *
     * Null if not set
     */
    var name: String? = name
        internal set

    /** Thumbnail Hash
     *
     * null = never given (e.g. veadotube mini 2.0), empty = none (e.g. state with no image in veado full)
     */
    var thumbHash: String? = thumbHash
        internal set

    /**
     * Updates stored name/thumbHash from State if ID matches and name isn't blank
     * @return true if [name] or [thumbHash] was updated
     */
    internal fun update(state: State): Boolean {
        var updated = false
        if (state.id == id) {
            if (name != state.name && state.name.isNotBlank()) {
                name = state.name
                updated = true
            }
            if (thumbHash != state.thumbHash && !state.thumbHash.isNullOrBlank()) {
                thumbHash = state.thumbHash
                updated = true
            }
        }
        return updated
    }

    /**
     * Updates stored PNG from State
     *
     * @return true if [thumbnail] was updated
     */
    internal fun update(state: ResultPayloadPng): Boolean = updateThumbnail(state)

    /**
     * Updates stored PNG
     *
     * @return true if [thumbnail] was updated
     */
    internal fun updateThumbnail(newThumbnail: ResultPayloadPng): Boolean {
        if (newThumbnail.state != id) return false

        // Get Existing thumbnail obj, create new if null
        when (val thumb = _thumbnail?.get()) {
            null -> VeadoThumbnail(newThumbnail).also {
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
    private var _thumbnail: WeakReference<VeadoThumbnail>? = null

    /**
     * State Thumbnail
     *
     * Base64 PNG
     *
     * May be `null` even if previously fetched and removed from Cache and cleaned up by GC
     *
     */
    val thumbnail: VeadoThumbnail?
        get() = _thumbnail?.get()

}

