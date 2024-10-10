package io.github.dissonantau.bleatkan.instance


import io.github.dissonantau.bleatkan.connection.Connection
import io.github.dissonantau.bleatkan.connection.ConnectionListener
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


/**
 * Represents an Instance
 *
 * Holds *ID* [InstanceID], *Server*, & *Name* values.
 * Can Generate a WebSocket [URI]
 *
 * InstancesManager uses lastModified in the extended contructior for storing the timestamp in the Instance File, and for clearing stale Instances
 *
 * Instance(id = [InstanceID], name = [String], server = [String], lastModified = [Long]) should be preferred
 *
 * Originally Based on [Veadotube bleatcan Instance.cs on Gitlab](https://gitlab.com/veadotube/bleatcan/-/blob/b1d4faf70138c1e839b449c3cf799b6fd59c837b/bleatcan/Instance.cs)
 *
 * @param id [InstanceID] for Instance
 * @param name Name of Instance (Title Name from Instance File)
 * @param server Server connection Address & Port
 *
 * @see InstanceID
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
data class Instance(
    /**
     * Instance ID for this Instance (from File Name)
     *
     * @see InstanceID
     */
    val id: InstanceID,
    /**
     * Client Display Name (For example "veadotube mini")
     *
     * Unlikely to change, but can if Server in Veadotube is changed during run.
     *
     * In this case the connection may close, or may not fail until next request
     * The instance file will update, but connections may need updating, closing, reopening, etc.
     */
    val name: String,
    /**
     * Server IP and Port separated with a colon (For example "127.0.0.1:12345")
     *
     * Unlikely to change, but can if Server in Veadotube is changed during run.
     *
     * In this case the connection will likely close, but may not fail until next request.
     * The instance file will update, but connections may need updating, closing, reopening, etc.
     */
    val server: String
) {


    /**
     * Unix Timestamp, Long
     * Last retrieved Time Value from Instance file
     * Anything older than 10 seconds should be assumed dead and removed
     */
    var fileLastModified: Long = Long.MIN_VALUE
        internal set


    /**
     * ID Unique to this Instance combining Server, Name, and ID.
     *
     * This String is Unique to the Veadotube Instance and Name, and excludes the fileLastModified value.
     *
     * A new Instance created with the same inputs would have the same instanceConnectionID
     */
    val instanceConnectionID: String


    init {
        require(id.type.isNotEmpty()) { "InstanceID is not Valid" }
        require(name.isNotBlank()) { "instanceName is blank" }
        require(server.isNotBlank()) { "serverAddress is blank" }

        instanceConnectionID = "$name-${server}_${id}"
    }

    constructor(id: InstanceID, name: String, server: String, lastModified: Long) : this(
        id = id,
        name = name,
        server = server
    ) {
        fileLastModified = lastModified
    }

    /**
     * Returns the URI for this Instance
     * This is one-to-one for Veadotube Mini
     *
     * @return URI for Instance & Client. Characters encoded as needed (e.g. "ws://127.0.0.1:12345?n=veadotube%20mini")
     */
    fun getWebSocketUri(): URI {
        return getWebSocketUri(server, name)
    }

    /**
     * Returns the URI for the given Client Name on this Instance
     * This is one-to-one for Veadotube Mini
     *
     * @param name Client Display Name (e.g. "veadotube mini")
     * @return URI for Instance & Client. Characters encoded as needed (e.g. "ws://127.0.0.1:12345?n=veadotube%20mini")
     */

    fun getWebSocketUri(name: String): URI {
        require(name.isNotBlank()) { "Name must not be blank" }
        return getWebSocketUri(server, name)
    }

    /**
     * Returns a new Connection on the Instance Server and Name for the Given Receiver
     *
     * Equivalent of Connection(instance (this), receiver)
     *
     * @param listener Object to be sent events by Connection Object
     * @return Connection
     */
    fun connect(listener: ConnectionListener): Connection {
        return Connection(this, listener)
    }

    companion object {
        /**
         * Returns the URI for the given Client Name on this Instance
         * This is one-to-one for Veadotube Mini
         *
         * @param server Server IP and Port separated with a colon (e.g. "127.0.0.1:12345")
         * @param name   Client Display Name (e.g. "veadotube mini")
         * @return URI for Instance & Client. Characters encoded as needed (e.g. "ws://127.0.0.1:12345?n=veadotube%20mini")
         */
        @JvmStatic
        fun getWebSocketUri(server: String, name: String): URI {
            require(server.isNotBlank()) { "Server can not be empty or blank" }
            require(name.isNotBlank()) { "Name can not be empty or blank" }

            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
            return URI("ws://$server?n=$encodedName")

        }
    }
}
