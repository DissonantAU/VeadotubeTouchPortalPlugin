package io.github.dissonantau.veadotubetouchportalplugin.data

import io.github.dissonantau.bleatkan.message.ResultPayload.ResultPayloadPng

/** Represents a Veadotube Thumbnail */
@Suppress("MemberVisibilityCanBePrivate")
class VeadoThumbnail(thumbnail: ResultPayloadPng) {

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
    internal fun updateThumbnail(thumbnail: ResultPayloadPng): Boolean {
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