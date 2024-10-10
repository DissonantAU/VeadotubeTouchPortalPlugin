@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.dissonantau.bleatkan.message


import kotlinx.serialization.*
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject


/* Request Message */

object RequestMessageSerializer : JsonContentPolymorphicSerializer<RequestMessage>(RequestMessage::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RequestMessage> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject["event"]?.equals("list") ?: false -> RequestMessage.RequestMessageNodeList.serializer()
            jsonObject.containsKey("payload") -> RequestMessage.RequestMessageNodeEvent.serializer()
            else -> throw IllegalArgumentException("Unsupported request type")
        }
    }
}


object RequestPayloadSerializer : JsonContentPolymorphicSerializer<RequestPayload>(RequestPayload::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RequestPayload> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("token") -> RequestPayload.RequestPayloadEventToken.serializer()
            jsonObject.containsKey("state") -> RequestPayload.RequestPayloadEventState.serializer()
            jsonObject.containsKey("event") -> RequestPayload.RequestPayloadEvent.serializer()
            else -> throw IllegalArgumentException("Unsupported Payload type")
        }
    }
}


/* Result Message */

object ResultMessageSerializer : JsonContentPolymorphicSerializer<ResultMessage>(ResultMessage::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ResultMessage> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("payload") -> ResultMessage.ResultMessageWithPayload.serializer()
            jsonObject.containsKey("entries") -> ResultMessage.ResultMessageWithEntryList.serializer()
            else -> throw IllegalArgumentException("Unsupported Payload type")
        }
    }
}


object ResultPayloadSerializer : JsonContentPolymorphicSerializer<ResultPayload>(ResultPayload::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ResultPayload> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("states") -> ResultPayload.ResultPayloadStateList.serializer()
            jsonObject.containsKey("state") -> when {
                jsonObject.containsKey("png") -> ResultPayload.ResultPayloadPng.serializer()
                else -> ResultPayload.ResultPayloadState.serializer()
            }

            else -> throw IllegalArgumentException("Unsupported Payload type")
        }
    }
}
