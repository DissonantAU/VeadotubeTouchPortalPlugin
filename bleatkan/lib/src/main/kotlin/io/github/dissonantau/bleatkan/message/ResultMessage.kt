@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.dissonantau.bleatkan.message


import io.ktor.util.*
import kotlinx.serialization.*


/**
 * Result Message
 *
 * Generic Class - not used directly
 *
 * @see ResultMessageWithEntryList
 * @see ResultMessageWithEntryList
 */
@Serializable(ResultMessageSerializer::class)
sealed class ResultMessage {
    //e.g. Current State, List of States, State Thumbnail
    abstract val event: String

    /**
     * Result Message with a Payload
     */
    @Serializable
    data class ResultMessageWithPayload(
        override val event: String,
        /** Type - e.g. stateEvents */
        val type: String,
        /** ID - e.g. mini */
        val id: String,
        /** Name - e.g. avatar state */
        val name: String,
        /** Payload - e.g. Current State, List of States, State Thumbnail */
        val payload: ResultPayload
    ) : ResultMessage()

    /**
     * Result Message with a List of Entries
     */
    @Serializable
    data class ResultMessageWithEntryList(
        override val event: String,
        val entries: List<Entry>
    ) : ResultMessage()

    /**
     * Channel message was received from
     *
     * Transient value not included in JSON, but is added after decoding for use if needed
     *
     * Blank by default
     */
    @Transient
    var channel: String = ""
        internal set

}

/**
 * Result Payload
 *
 * e.g. Current State, List of States, State Thumbnail
 *
 * Generic Class - not used directly
 *
 * @see ResultPayloadState
 * @see ResultPayloadStateList
 * @see ResultPayloadPng
 */
@Serializable(ResultPayloadSerializer::class)
sealed class ResultPayload {
    abstract val event: String

    /** Payload with a List of States - e.g. List of Avatar States */
    @Serializable
    data class ResultPayloadStateList(
        override val event: String,
        val states: List<State>
    ) : ResultPayload()

    /** Payload with a Single State - e.g. Current Avatar State */
    @Serializable
    data class ResultPayloadState(
        override val event: String,
        val state: String
    ) : ResultPayload()

    /** Payload with a State Thumbnail - e.g. Avatar State Thumbnail */
    @Serializable
    data class ResultPayloadPng(
        override val event: String,
        val state: String,
        /**
         * PNG Width in Pixels
         */
        val width: Int,
        /**
         * PNG Height in Pixels
         */
        val height: Int,
        /**
         * PNG Encoded as a Base64 Encoded String
         *
         * @see pngAsBytes to get PNG as a decoded ByteArray
         */
        val png: String
    ) : ResultPayload() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ResultPayloadPng

            if (event != other.event) return false
            if (state != other.state) return false
            if (width != other.width) return false
            if (height != other.height) return false
            if (!png.contentEquals(other.png)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = event.hashCode()
            result = 31 * result + state.hashCode()
            result = 31 * result + width
            result = 31 * result + height
            result = 31 * result + png.hashCode()
            return result
        }

        /**
         * Encodes PNG as Base64 Encoded String
         * @return PNG encoded as a Base64 String
         */
        fun pngAsString(): String = png


        /**
         * Returns a copy of the PNG Byte Array
         * @return PNG as a Byte Array
         */
        fun pngAsBytes(): ByteArray = png.decodeBase64Bytes()


        override fun toString(): String {
            //
            return "ResultPayloadPng(event='$event', state='$state', width=$width, height=$height, png={hash=${png.hashCode()}, count=${png.count()}})"
        }
    }
}


@Serializable
data class State(
    val id: String,
    val name: String
)


@Serializable
data class Entry(
    val type: String,
    val id: String,
    val name: String
)
