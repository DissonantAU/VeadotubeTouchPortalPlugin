package io.github.dissonantau.veadotubetouchportalplugin

import io.github.dissonantau.bleatkan.connection.Connection
import io.github.dissonantau.bleatkan.message.MessageEvent
import io.github.dissonantau.bleatkan.message.MessagePayloadId
import io.github.dissonantau.bleatkan.message.MessagePayloadType
import io.github.dissonantau.bleatkan.message.PayloadEvent
import io.github.dissonantau.bleatkan.message.RequestMessage
import io.github.dissonantau.bleatkan.message.RequestMessage.RequestMessageNodeEvent
import io.github.dissonantau.bleatkan.message.RequestPayload
import io.github.dissonantau.bleatkan.message.RequestPayload.RequestPayloadEventToken
import io.github.dissonantau.bleatkan.message.VeadoRequest
import io.github.dissonantau.bleatkan.message.VeadoRequest.FACTORY.createPayload
import io.github.dissonantau.bleatkan.message.VeadoRequest.FACTORY.createRequest
import io.github.dissonantau.bleatkan.message.VeadoRequest.FACTORY.getPayloadEventGet
import io.github.dissonantau.bleatkan.message.VeadoRequest.FACTORY.getPayloadEventList
import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.Companion.CHANNEL_INSTANCE
import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.Companion.CHANNEL_NODES
import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.Companion.LISTENER_TOKEN_BASE
import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.Companion.LISTENER_TOKEN_CHANGESTATE
import io.github.dissonantau.veadotubetouchportalplugin.VeadoTouchPlugin.Companion.LOGGER
import io.github.dissonantau.veadotubetouchportalplugin.data.VeadoStateNodeData


/* Veado Requests */
internal fun sendRequestInstanceInfo(connection: Connection) = try {
    connection.send(channel = CHANNEL_INSTANCE, requestData = VeadoRequest.getEventInfo)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request Instance Info for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestNodeList(connection: Connection) = try {
    connection.send(channel = CHANNEL_NODES, requestData = VeadoRequest.getEventList)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request Node List for ${connection.connUri}: ${ex.message}" }
}

internal fun sendChannelListenerEnable(connection: Connection, channel: String = CHANNEL_NODES) = try {
    //println("Send enable nodes listener")
    connection.send(
        channel = channel, RequestMessage.RequestMessageNodeEventToken(
            event = PayloadEvent.LISTEN.formattedName, token = "$LISTENER_TOKEN_BASE.$CHANNEL_NODES"
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Enable Node Listener for ${connection.connUri}; ${channel}: ${ex.message}" }
}

internal fun sendChannelListenerDisable(connection: Connection, channel: String = CHANNEL_NODES) = try {
    //println("Send enable nodes listener")
    connection.send(
        channel = channel, RequestMessage.RequestMessageNodeEventToken(
            event = PayloadEvent.UNLISTEN.formattedName, token = "$LISTENER_TOKEN_BASE.$CHANNEL_NODES"
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Disable Node Listener for ${connection.connUri}; ${channel}: ${ex.message}" }
}

internal fun sendRequestNodeStateEventsList(
    connection: Connection, channel: String = CHANNEL_NODES, nodeId: String = MessagePayloadId.MINI.formattedName
) = try {
    connection.send(
        channel = channel, requestData = RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = MessagePayloadType.STATE_EVENTS.formattedName,
            id = nodeId, payload = getPayloadEventList
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request State List for ${connection.connUri}; ${channel}; ${nodeId}: ${ex.message}" }
}

internal fun sendRequestNodeNumberGet(connection: Connection, channel: String = CHANNEL_NODES, nodeId: String) = try {
    connection.send(
        channel = channel, requestData = RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = MessagePayloadType.NUMBER.formattedName,
            id = nodeId, payload = getPayloadEventGet
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request State List for ${connection.connUri}; ${channel}; ${nodeId}: ${ex.message}" }
}

internal fun sendRequestNodeBooleanGet(connection: Connection, channel: String = CHANNEL_NODES, nodeId: String) = try {
    connection.send(
        channel = channel, requestData = RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = MessagePayloadType.BOOLEAN.formattedName,
            id = nodeId, payload = getPayloadEventGet
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Get Current State for ${connection.connUri}; ${channel}; ${nodeId}: ${ex.message}" }
}

internal fun sendRequestMiniAvatarStateList(connection: Connection) = try {
    connection.send(channel = CHANNEL_NODES, requestData = VeadoRequest.getListStateMini)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request State List for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestMiniAvatarStatePeek(connection: Connection) = try {
    connection.send(channel = CHANNEL_NODES, requestData = VeadoRequest.getPeekStateMini)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request Current State for ${connection.connUri}: ${ex.message}" }
}


internal fun sendRequestMiniPushToTalkMicGet(connection: Connection) = try {
    connection.send(channel = CHANNEL_NODES, requestData = VeadoRequest.getValuePttMini)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request Current State for ${connection.connUri}: ${ex.message}" }
}

/**
 * Send request for Push-To-Talk Mic Input
 *
 * Warning:
 * * Enabling Push-to-Talk 'Mutes' the Microphone input
 * until an API message to toggle/unmute is sent
 * * If a hotkey is set, API won't work
 *
 * @param sendValue
 * * true > PPT Node 'enabled' > Mic Unmuted
 * * false > PPT Node 'disabled' > Mic Muted
 * * null > Toggle
 */
internal fun sendRequestMiniPushToTalkMicInput(connection: Connection, sendValue: Boolean? = null) = try {
    val pushToTalk = if (sendValue != null) {
        VeadoRequest.createRequestWithPayload(
            event = MessageEvent.PAYLOAD, type = MessagePayloadType.BOOLEAN,
            id = MessagePayloadId.MINI.formattedName,
            payloadEvent = PayloadEvent.SET, payloadValue = sendValue
        )
    } else {
        VeadoRequest.createRequestWithPayload(
            event = MessageEvent.PAYLOAD, type = MessagePayloadType.BOOLEAN,
            id = MessagePayloadId.MINI.formattedName, payloadEvent = PayloadEvent.TOGGLE
        )
    }
    LOGGER.trace { "sendRequestMiniPushToTalkMicInput: $pushToTalk" }
    connection.send(channel = CHANNEL_NODES, requestData = pushToTalk)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Set Push To Talk for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestMiniStateThumbnail(connection: Connection, stateID: String) = try {
    connection.send(
        channel = CHANNEL_NODES, requestData = VeadoRequest.createThumbnailStateMini(stateID)
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request Current State Thumbnail for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestMiniStartListener(connection: Connection) = try {
    connection.send(
        channel = CHANNEL_NODES, VeadoRequest.createListenStateMini(LISTENER_TOKEN_CHANGESTATE)
    )
    connection.send(
        channel = CHANNEL_NODES, VeadoRequest.createListenPttMini(LISTENER_TOKEN_CHANGESTATE)
    )
} catch (ex: Exception) {
    LOGGER.debug { "Failed to Request Listener Start for ${connection.connUri}: ${ex.message}; ${ex.stackTraceToString()}" }
}

internal fun sendRequestMiniStopListener(connection: Connection) = try {
    connection.send(
        channel = CHANNEL_NODES, VeadoRequest.createUnlistenStateMini(LISTENER_TOKEN_CHANGESTATE)
    )
    connection.send(
        channel = CHANNEL_NODES, VeadoRequest.createUnlistenPttMini(LISTENER_TOKEN_CHANGESTATE)
    )
} catch (ex: Exception) {
    LOGGER.debug { "Failed to Request Listener Stop for ${connection.connUri}: ${ex.message}; ${ex.stackTraceToString()}" }
}

/**
 * Send request for Push-To-Talk Mic Input
 *
 * Warning:
 * * Enabling Push-to-Talk 'Mutes' the Microphone input
 * until an API message to toggle/unmute is sent
 * * If a hotkey is set, API won't work
 *
 * @param change [PayloadEvent]
 * * [PayloadEvent.SET]
 * * [PayloadEvent.TOGGLE]
 */
internal fun sendRequestChangeBooleanNode(connection: Connection, nodeId: String, change: PayloadEvent) = try {
    val pushToTalk = VeadoRequest.createRequestWithPayload(
        event = MessageEvent.PAYLOAD, type = MessagePayloadType.BOOLEAN, id = nodeId,
        payloadEvent = PayloadEvent.SET, payloadValue = change.formattedName
    )

    LOGGER.trace { "sendRequestSetBooleanNode: $pushToTalk" }
    connection.send(channel = CHANNEL_NODES, requestData = pushToTalk)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to ${change.formattedName} Boolean Node $nodeId for ${connection.connUri}: ${ex.message}" }
}

/**
 * Send request for Push-To-Talk Mic Input
 *
 * Warning:
 * * Enabling Push-to-Talk 'Mutes' the Microphone input
 * until an API message to toggle/unmute is sent
 * * If a hotkey is set, API won't work
 *
 * @param sendValue
 * - true, false
 * - to Toggle, use [sendRequestToggleBooleanNode]
 */
internal fun sendRequestSetBooleanNode(connection: Connection, nodeId: String, sendValue: Boolean) = try {
    val pushToTalk = VeadoRequest.createRequestWithPayload(
        event = MessageEvent.PAYLOAD, type = MessagePayloadType.BOOLEAN, id = nodeId,
        payloadEvent = PayloadEvent.SET, payloadValue = sendValue
    )
    LOGGER.trace { "sendRequestSetBooleanNode: $pushToTalk" }
    connection.send(channel = CHANNEL_NODES, requestData = pushToTalk)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Set Boolean Node $nodeId to $sendValue for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestToggleBooleanNode(connection: Connection, nodeId: String) = try {
    val pushToTalk = VeadoRequest.createRequestWithPayload(
        event = MessageEvent.PAYLOAD, type = MessagePayloadType.BOOLEAN, id = nodeId,
        payloadEvent = PayloadEvent.TOGGLE
    )
    LOGGER.trace { "sendRequestToggleBooleanNode: $pushToTalk" }
    connection.send(channel = CHANNEL_NODES, requestData = pushToTalk)
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Toggle Boolean Node $nodeId for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestFullStateThumbnail(connection: Connection, nodeId: String, stateID: String) = try {
    connection.send(
        channel = CHANNEL_NODES, requestData = createRequest(
            event = MessageEvent.PAYLOAD, type = MessagePayloadType.STATE_EVENTS, id = nodeId,
            payload = createPayload(event = PayloadEvent.THUMB, value = stateID)
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request Thumbnail for State $stateID in $nodeId, ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestFullStartListener(connection: Connection, nodeType: String, nodeId: String) = try {
    connection.send(
        channel = CHANNEL_NODES,
        RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = nodeType, id = nodeId,
            payload = RequestPayloadEventToken(
                event = PayloadEvent.LISTEN.formattedName, token = LISTENER_TOKEN_CHANGESTATE
            )
        )
    )
} catch (ex: Exception) {
    LOGGER.debug { "Failed to Request Listener Start for ${connection.connUri}: ${ex.message}; ${ex.stackTraceToString()}" }
}

internal fun sendRequestFullStopListener(connection: Connection, nodeType: String, nodeId: String) = try {
    connection.send(
        channel = CHANNEL_NODES, requestData = RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = nodeType, id = nodeId,
            payload = RequestPayloadEventToken(
                event = PayloadEvent.UNLISTEN.formattedName, token = LISTENER_TOKEN_CHANGESTATE
            )
        )
    )
} catch (ex: Exception) {
    LOGGER.debug { "Failed to Request Listener Stop for ${connection.connUri}: ${ex.message}; ${ex.stackTraceToString()}" }
}

internal fun sendRequestStateList(connection: Connection, node: VeadoStateNodeData) = try {
    connection.send(
        channel = CHANNEL_NODES, requestData = RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = node.type, id = node.id,
            payload = RequestPayload.RequestPayloadEvent(event = PayloadEvent.LIST.formattedName)
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request State List for ${connection.connUri}: ${ex.message}" }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun sendRequestStateListPeek(connection: Connection, nodeId: String)  {
    sendRequestStateList(connection,nodeId)
    sendRequestStatePeek(connection,nodeId)
}

internal fun sendRequestStateList(connection: Connection, nodeId: String) = try {
    connection.send(
        channel = CHANNEL_NODES, requestData = RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = "stateEvents", id = nodeId,
            payload = RequestPayload.RequestPayloadEvent(event = PayloadEvent.LIST.formattedName)
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request State List for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestStatePeek(connection: Connection, nodeId: String) = try {
    connection.send(
        channel = CHANNEL_NODES, RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = "stateEvents", id = nodeId,
            payload = RequestPayload.RequestPayloadEvent(event = PayloadEvent.PEEK.formattedName)
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request Current State for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestMiniStateList(connection: Connection) = try {
    connection.send(
        channel = CHANNEL_NODES, requestData = RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = "stateEvents", id = "mini",
            payload = RequestPayload.RequestPayloadEvent(event = PayloadEvent.LIST.formattedName)
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request State List for ${connection.connUri}: ${ex.message}" }
}

internal fun sendRequestMiniStatePeek(connection: Connection) = try {
    connection.send(
        channel = CHANNEL_NODES,
        RequestMessageNodeEvent(
            event = MessageEvent.PAYLOAD.formattedName, type = "stateEvents", id = "mini",
            payload = RequestPayload.RequestPayloadEvent(event = PayloadEvent.PEEK.formattedName)
        )
    )
} catch (ex: Exception) {
    LOGGER.warn { "Failed to Request Current State for ${connection.connUri}: ${ex.message}" }
}