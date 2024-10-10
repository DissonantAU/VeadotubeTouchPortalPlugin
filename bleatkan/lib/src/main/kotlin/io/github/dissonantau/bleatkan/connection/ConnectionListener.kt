package io.github.dissonantau.bleatkan.connection


import io.github.dissonantau.bleatkan.message.ResultMessage


interface ConnectionListener {

    /**
     * Veadotube Connection Event - Connection Error
     *
     * @param connection Connection providing update
     * @param error Error Type
     */
    fun onConnectionError(connection: Connection, error: ConnectionError)

    /**
     * Veadotube Connection Event - Connection Active (Connect/Up) or Inactive (Disconnect/Down)
     *
     * @param connection Connection providing update
     * @param active Connection active/inactive
     */
    fun onConnectionChange(connection: Connection, active: Boolean)

    /**
     * Veadotube Connection Event - Message Received
     *
     * @param connection Connection providing update
     * @param message Message Received
     */
    fun onConnectionReceive(connection: Connection, message: ResultMessage)
}
