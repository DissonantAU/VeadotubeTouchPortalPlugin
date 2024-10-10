package io.github.dissonantau.bleatkan.message


import kotlinx.serialization.*


/**
 * Represents an instance file's contents
 */
@Serializable
data class VtInstance(
    /** Instance File Update Time */
    var time: Long,
    /** Instance name/title */
    val name: String,
    /** Instance Server address - *IP:Port* */
    val server: String
)
