@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package io.github.dissonantau.bleatkan.message


import kotlinx.serialization.*
import kotlinx.serialization.json.Json


/**
 * Factory for Building and Validating Request Messages.
 * Returns an Object that can be serialized to JSON.
 * Does not include a channel prefix (ie "nodes:")
 *
 * Common Reusable/Immutable Request and Payload Objects can be gotten from the Factory.
 * These objects are Lazy Initialised, and are also returned by build functions when possible
 */
class VtRequest {

    companion object FACTORY {

        /* Common Requests that can be reused - using lazy initialisation to only create when first accessed */
        /** Request of *event: list* - Lazy Initialized equivalent of *[getPayloadEventList] as VtRequest* */
        @JvmStatic
        val getEventList: RequestMessage by lazy {
            RequestMessage.RequestMessageNodeList(event = MessageEvent.LIST.value)
        }

        /** Request Payload of *event: list* */
        @JvmStatic
        val getPayloadEventList: RequestPayload by lazy {
            RequestPayload.RequestPayloadEvent(event = PayloadEvent.LIST.value)
        }

        /** Request Payload of *event: peek* */
        @JvmStatic
        val getPayloadEventPeek: RequestPayload by lazy {
            RequestPayload.RequestPayloadEvent(event = PayloadEvent.PEEK.value)
        }

        /** Common Prebuilt Request to get Avatar State List from Veadotube Mini
         *
         * Equivalent of
         * createRequest(
         *  event = [MessageEvent.PAYLOAD],
         *  type = [MessagePayloadType.STATE_EVENTS],
         *  id = [MessagePayloadId.MINI],
         *  payload = [getPayloadEventList]
         * )
         *
         * @see createRequest
         * @see getPayloadEventList
         * */
        @JvmStatic
        val getListStateMini: RequestMessage by lazy {
            createRequest(payload = getPayloadEventList)
        }

        /** Common Prebuilt Request to get (peek) the current Avatar State from Veadotube Mini
         *
         * Equivalent of
         * createRequest(
         *  event = [MessageEvent.PAYLOAD],
         *  type = [MessagePayloadType.STATE_EVENTS],
         *  id = [MessagePayloadId.MINI],
         *  payload = [getPayloadEventPeek]
         * )
         *
         * @see createRequest
         * @see getPayloadEventPeek
         * */
        @JvmStatic
        val getPeekStateMini: RequestMessage by lazy {
            createRequest(payload = getPayloadEventPeek)
        }

        /**
         * Returns a Request [RequestMessage] with a Set State Payload [RequestPayload]
         *
         * Convenience Function to build a Set Avatar State Request
         *
         * Equivalent of
         * createRequest(
         *  event = [MessageEvent.PAYLOAD],
         *  type = [MessagePayloadType.STATE_EVENTS],
         *  id = [MessagePayloadId.MINI],
         *  payload = createPayload(
         *   event = [PayloadEvent.SET],
         *   value = [stateID]
         *  )
         * )
         *
         * @see createRequest
         * @see createPayload
         *
         */
        @JvmStatic
        fun createSetStateMini(
            stateID: String
        ) = createRequest(
            event = MessageEvent.PAYLOAD,
            type = MessagePayloadType.STATE_EVENTS,
            id = MessagePayloadId.MINI,
            payload = createPayload(
                event = PayloadEvent.SET,
                value = stateID
            )
        )

        /**
         * Returns a Request [RequestMessage] with a Push State Payload[RequestPayload]
         *
         * Convenience Function to build a Push Avatar State Request
         *
         * Equivalent of
         * createRequest(
         *  event = [MessageEvent.PAYLOAD],
         *  type = [MessagePayloadType.STATE_EVENTS],
         *  id = [MessagePayloadId.MINI],
         *  payload = createPayload(
         *   event = [PayloadEvent.PUSH],
         *   value = [stateID]
         *  )
         * )
         *
         * @see createRequest
         * @see createPayload
         *
         */
        @JvmStatic
        fun createPushStateMini(
            stateID: String
        ) = createRequest(
            event = MessageEvent.PAYLOAD,
            type = MessagePayloadType.STATE_EVENTS,
            id = MessagePayloadId.MINI,
            payload = createPayload(
                event = PayloadEvent.PUSH,
                value = stateID
            )
        )

        /**
         * Returns a Request [RequestMessage] with a Pop State Payload [RequestPayload]
         *
         * Convenience Function to build a Pop Avatar State Request
         *
         * Equivalent of
         * createRequest(
         *  event = [MessageEvent.PAYLOAD],
         *  type = [MessagePayloadType.STATE_EVENTS],
         *  id = [MessagePayloadId.MINI],
         *  payload = createPayload(
         *   event = [PayloadEvent.POP],
         *   value = [stateID]
         *  )
         * )
         *
         * @see createRequest
         * @see createPayload
         *
         */
        @JvmStatic
        fun createPopStateMini(
            stateID: String
        ) = createRequest(
            event = MessageEvent.PAYLOAD,
            type = MessagePayloadType.STATE_EVENTS,
            id = MessagePayloadId.MINI,
            payload = createPayload(
                event = PayloadEvent.POP,
                value = stateID
            )
        )

        /**
         * Returns a Request [RequestMessage] with a Listen State Payload [RequestPayload]
         *
         * Convenience Function to build a Listen Avatar State Request
         *
         * Equivalent of
         * createRequest(
         *  event = [MessageEvent.PAYLOAD],
         *  type = [MessagePayloadType.STATE_EVENTS],
         *  id = [MessagePayloadId.MINI],
         *  payload = createPayload(
         *   event = [PayloadEvent.LISTEN],
         *   value = [stateID]
         *  )
         * )
         *
         * @param stateID Avatar State ID
         *
         * @see createRequest
         * @see createPayload
         *
         */
        @JvmStatic
        fun createListenStateMini(
            stateID: String
        ) = createRequest(
            event = MessageEvent.PAYLOAD,
            type = MessagePayloadType.STATE_EVENTS,
            id = MessagePayloadId.MINI,
            payload = createPayload(
                event = PayloadEvent.LISTEN,
                value = stateID
            )
        )

        /**
         * Returns a Request [RequestMessage] with an Unlisten State Payload [RequestPayload]
         *
         * Convenience Function to build an Unlisten Avatar State Request
         *
         * Equivalent of
         * createRequest(
         *  event = [MessageEvent.PAYLOAD],
         *  type = [MessagePayloadType.STATE_EVENTS],
         *  id = [MessagePayloadId.MINI],
         *  payload = createPayload(
         *   event = [PayloadEvent.UNLISTEN],
         *   value = [stateID]
         *  )
         * )
         *
         * @param stateID Avatar State ID
         *
         * @see createRequest
         * @see createPayload
         *
         */
        @JvmStatic
        fun createUnlistenStateMini(
            stateID: String
        ) = createRequest(
            event = MessageEvent.PAYLOAD,
            type = MessagePayloadType.STATE_EVENTS,
            id = MessagePayloadId.MINI,
            payload = createPayload(
                event = PayloadEvent.UNLISTEN,
                value = stateID
            )
        )


        /**
         * Returns a Request [RequestMessage] with a Thumbnail State Payload [RequestPayload]
         *
         * Convenience Function to build a Thumbnail Avatar State Request
         *
         * Equivalent of
         * createRequest(
         *  event = [MessageEvent.PAYLOAD],
         *  type = [MessagePayloadType.STATE_EVENTS],
         *  id = [MessagePayloadId.MINI],
         *  payload = createPayload(
         *   event = [PayloadEvent.THUMB],
         *   value = [stateID]
         *  )
         * )
         *
         * @see createRequest
         * @see createPayload
         *
         */
        @JvmStatic
        fun createThumbnailStateMini(
            stateID: String
        ) = createRequest(
            event = MessageEvent.PAYLOAD,
            type = MessagePayloadType.STATE_EVENTS,
            id = MessagePayloadId.MINI,
            payload = createPayload(
                event = PayloadEvent.THUMB,
                value = stateID
            )
        )


        /* Builder Functions */
        /**
         * Returns a Request that inherits [RequestMessage]
         *
         * Common Uses:
         *
         * [event] = [MessageEvent.LIST]
         * * Doesn't require any other parameters, anything provided will be ignored.
         * * Returns [VtRequest.getEventList] - it can be accessed directly, and should be used instead if possible
         * * (See [RequestPayload.RequestPayloadEvent])
         *
         *
         * [event] = [MessageEvent.PAYLOAD]
         * * Uses [type], [id], and [payload] must be provided
         * * All must be non-blank and non-null.
         * * (See [RequestPayload.RequestPayloadEventToken])
         *
         *
         * @see [RequestPayload.RequestPayloadEvent]
         * @see [RequestPayload.RequestPayloadEventToken]
         *
         */
        @JvmOverloads
        @JvmStatic
        fun createRequest(
            event: MessageEvent = MessageEvent.PAYLOAD,
            type: MessagePayloadType? = MessagePayloadType.STATE_EVENTS,
            id: MessagePayloadId? = MessagePayloadId.MINI,
            payload: RequestPayload? = null
        ): RequestMessage {
            val newRequest: RequestMessage = when (event) {
                /* List as base event is just a payload an Event Payload */
                MessageEvent.LIST -> {
                    /*Return Common/Reusable Object*/
                    getEventList
                }

                MessageEvent.PAYLOAD -> {
                    when (type) {

                        MessagePayloadType.STATE_EVENTS -> {
                            require(id != null) { "Type cannot be Null for Request ${MessageEvent.PAYLOAD} with Type ${MessagePayloadType.STATE_EVENTS}" }
                            require(payload != null) { "Payload cannot be Null for Request ${MessageEvent.PAYLOAD} with Type ${MessagePayloadType.STATE_EVENTS}" }

                            RequestMessage.RequestMessageNodeEvent(
                                event = event.value,
                                type = type.value,
                                id = id.value,
                                payload = payload
                            )
                        }

                        /* Type not specified - invalid */
                        null -> {
                            throw IllegalArgumentException("Type cannot be Null for Request ${MessageEvent.PAYLOAD}")
                        }

                        MessagePayloadType.UNKNOWN -> {
                            throw IllegalArgumentException("Type cannot be UNKNOWN")
                        }
                    }
                }

                else -> {
                    throw IllegalArgumentException("Event can't be $event, must be ${MessageEvent.LIST} or ${MessageEvent.PAYLOAD}")
                }

            }

            return newRequest
        }


        /**
         * Returns a Payload that inherits [RequestPayload]
         *
         *
         * [event] = [PayloadEvent.LIST] or [PayloadEvent.PEEK]
         * * Doesn't require [value], anything provided will be ignored.
         * * A [PayloadEvent.LIST] Payload can also be sent without being put into a Request to get a list of nodes.
         * * Returns [VtRequest.getPayloadEventList] or [VtRequest.getPayloadEventPeek] respectively
         * - they can be accessed directly, and should be used instead if possible
         * * (See [RequestPayload.RequestPayloadEvent])
         *
         *
         * [event] = [PayloadEvent.LISTEN] & [PayloadEvent.UNLISTEN]
         * * Uses [value] as the Token.
         * * Must be non-blank and non-null.
         * * (See [RequestPayload.RequestPayloadEventToken])
         *
         *
         * [event] = [PayloadEvent.SET], [PayloadEvent.PUSH], [PayloadEvent.POP], & [PayloadEvent.THUMB]
         * * Uses [value] as the State ID.
         * * Should be a valid State ID from a [MessageEvent.PAYLOAD] / [MessagePayloadType.STATE_EVENTS] / [PayloadEvent.LIST] request.
         * * (See [RequestPayload.RequestPayloadEventState])
         *
         *
         * @param event event value, should match [PayloadEvent] (except [PayloadEvent.UNKNOWN])
         *
         *
         * @see [RequestPayload.RequestPayloadEvent]
         * @see [RequestPayload.RequestPayloadEventToken]
         *
         */
        @JvmOverloads
        @JvmStatic
        fun createPayload(event: PayloadEvent, value: String? = null): RequestPayload {
            require(event != PayloadEvent.UNKNOWN) { "Payload Event can't be UNKNOWN" }


            val newRequest: RequestPayload =

                when (event) {

                    PayloadEvent.LIST -> {
                        /*Return Common/Reusable Object*/
                        getPayloadEventList
                    }

                    PayloadEvent.PEEK -> {
                        /*Return Common/Reusable Object*/
                        getPayloadEventPeek
                    }

                    PayloadEvent.LISTEN, PayloadEvent.UNLISTEN -> {
                        require(!value.isNullOrBlank()) { "Token Value for Payload $event Event can't be Null or Blank" }
                        RequestPayload.RequestPayloadEventToken(
                            event = event.value,
                            token = value.trim()
                        )
                    }

                    PayloadEvent.SET, PayloadEvent.PUSH, PayloadEvent.POP, PayloadEvent.THUMB -> {
                        require(!value.isNullOrBlank()) { "State Value for Payload $event Event can't be Null or Blank" }
                        RequestPayload.RequestPayloadEventState(
                            event = event.value,
                            state = value.trim()
                        )
                    }

                    else -> {
                        throw IllegalArgumentException("Unknown Event: $event")
                    }

                }

            return newRequest
        }


        /**
         * Returns a Request that inherits [RequestMessage] with a Payload
         *
         * Convenience Function to combine [createRequest] and [createPayload]
         *
         *
         * Common Uses:
         *
         * [event] = [MessageEvent.LIST]
         * * Doesn't require any other parameters, anything provided will be ignored.
         * * Returns [VtRequest.getEventList] - it can be accessed directly, and should be used instead if possible
         * * (See [RequestPayload.RequestPayloadEvent])
         *
         *
         * [event] = [MessageEvent.PAYLOAD]
         * * Uses [type], [id], can be provided while [payloadEvent] must be provided, along with [payloadValue] if it's needed
         * * All must be non-blank and non-null.
         * * (See [RequestPayload.RequestPayloadEventToken])
         *
         *
         * @see [RequestPayload.RequestPayloadEvent]
         * @see [RequestPayload.RequestPayloadEventToken]
         * @see [createRequest]
         *
         */
        @JvmOverloads
        @JvmStatic
        fun createRequestWithPayload(
            event: MessageEvent = MessageEvent.PAYLOAD,
            type: MessagePayloadType? = MessagePayloadType.STATE_EVENTS,
            id: MessagePayloadId? = MessagePayloadId.MINI,
            payloadEvent: PayloadEvent, payloadValue: String? = null
        ) = createRequest(
            event = event,
            type = type,
            id = id,
            payload = createPayload(
                event = payloadEvent,
                value = payloadValue
            )
        )

    }

}


// Enum for Known Message Events
enum class MessageEvent(val value: String) {
    UNKNOWN("UNKNOWN"),
    ERROR("ERROR"),
    /* Message is targeting a specific node */
    /** For a Request that targets a node (eg stateEvents)*/
    @SerialName("payload")
    PAYLOAD("payload"),

    /* Requesting Values*/
    /** Requests a List of Possible Values*/
    @SerialName("list")
    LIST("list"),
    ;

    companion object {
        /**
         * Gets Enum Constant with given Value
         *
         * Returns UNKNOWN ENUM if not found
         *
         *@return Enum Constant
         */
        @JvmStatic
        fun fromValue(value: String): MessageEvent {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}


// Enum for Known Message Types
enum class MessagePayloadType(val value: String) {
    UNKNOWN("UNKNOWN"),

    @SerialName("stateEvents")
    STATE_EVENTS("stateEvents"),
    ;

    companion object {
        /**
         * Gets Enum Constant with given Value
         *
         * Returns UNKNOWN ENUM if not found
         *
         *@return Enum Constant
         */
        @JvmStatic
        fun fromValue(value: String): MessagePayloadType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}


// Enum for Known Message IDs
enum class MessagePayloadId(val value: String) {
    UNKNOWN("UNKNOWN"),

    @SerialName("mini")
    MINI("mini"),
    ;

    companion object {
        /**
         * Gets Enum Constant with given Value
         *
         * Returns UNKNOWN ENUM if not found
         *
         *@return Enum Constant
         */
        @JvmStatic
        fun fromValue(value: String): MessagePayloadId {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

// Enum for Known Payload Events
enum class PayloadEvent(val value: String) {
    UNKNOWN("UNKNOWN"),

    /* Requesting Values*/
    /** Requests a List of Possible Values*/
    @SerialName("list")
    LIST("list"),

    /** Requests current Single Value*/
    @SerialName("peek")
    PEEK("peek"),

    /** Requests the image related to a Possible Value*/
    @SerialName("thumb")
    THUMB("thumb"),

    /* Setting Values */
    /** Sets a Single Value*/
    @SerialName("set")
    SET("set"),

    /** Pushes a Single Value*/
    @SerialName("push")
    PUSH("push"),

    /** Pops the last Pushed Single Value*/
    @SerialName("pop")
    POP("pop"),

    /* Listening to Value Changes */
    /** Listens to changes to a Channel Value*/
    @SerialName("listen")
    LISTEN("listen"),

    /** Stops listening to changes to a Channel Value*/
    @SerialName("unlisten")
    UNLISTEN("unlisten"),
    ;

    companion object {
        /**
         * Gets Enum Constant with given Value
         *
         * Returns UNKNOWN ENUM if not found
         *
         *@return Enum Constant
         */
        @JvmStatic
        fun fromValue(value: String): PayloadEvent {
            return requireNotNull(entries.find { it.value == value }) { UNKNOWN }
        }
    }
}


/**
 * Class used to represent Veadotube Request Messages.
 *
 * The [validate] function can be used to help validate the message
 *
 * Must always have an [event] value at minimum
 */
@Serializable(RequestMessageSerializer::class)
sealed class RequestMessage {
    abstract val event: String

    /**
     * Message for Listing Nodes
     */
    @Serializable
    data class RequestMessageNodeList(
        override val event: String
    ) : RequestMessage()

    @Serializable
    data class RequestMessageNodeEvent(
        override val event: String,
        /**
         * Node Type to send the request to
         *
         * e.g. stateEvents
         */
        val type: String,
        /**
         * Node ID to send the request to
         *
         * e.g. mini
         */
        val id: String,
        /**
         * Payload to send to node [type] / [id]
         */
        val payload: RequestPayload
    ) : RequestMessage()

    fun toJsonString(): String {
        return Json.encodeToString(this)
    }

    companion object {
        /**
         * Validates Data in a Request.
         *
         * Checks the Data Class and contents to make sure the combination is valid using [PayloadEvent] Values.
         *
         * If the Request is a Payload, it will also be validated
         *
         * @param requestData data object inheriting [RequestMessage]
         *
         * @return true if tests passed
         * @throws IllegalStateException if any invalid value is found. Message contains details of issues
         *
         * @see RequestPayload.validate Use to validate Payload Contents if applicable
         */
        @Throws(IllegalStateException::class)
        @JvmStatic
        fun validate(requestData: RequestMessage): Boolean {
            val errorSb by lazy { StringBuilder().append { "Request: " } }
            var valid = true

            when {
                /* Check vs Type Block Start */
                (requestData is RequestMessageNodeList) -> {
                    /* Check VtRequestNodeList Block Start */
                    when (MessageEvent.fromValue(requestData.event)) {
                        MessageEvent.LIST -> {/*Valid, no more to do */
                        }

                        else -> {
                            valid = false
                            errorSb.append { "event (${requestData.event});" }
                        }
                    }
                    /* Check VtRequestNodeList Block End */
                }

                (requestData is RequestMessageNodeEvent) -> {
                    /* Check VtRequestNodeMessage Block Start */
                    when (MessageEvent.fromValue(requestData.event)) {
                        MessageEvent.PAYLOAD -> {
                            /*Valid, but other values need checking */

                            /* Check Request Values Start */
                            when (MessagePayloadType.fromValue(requestData.type)) {
                                MessagePayloadType.STATE_EVENTS -> {/*Valid, no more to do */
                                }

                                else -> {
                                    valid = false
                                    errorSb.append { "type (${requestData.type});" }
                                }
                            }

                            when (MessagePayloadId.fromValue(requestData.id)) {
                                MessagePayloadId.MINI -> {/*Valid, no more to do */
                                }

                                else -> {
                                    valid = false
                                    errorSb.append { "id (${requestData.id});" }
                                }
                            }

                            /*Payload Check*/
                            try {
                                RequestPayload.validate(requestData.payload)
                            } catch (ex: Exception) {
                                valid = false
                                errorSb.append { ex.message }
                            }

                            /* Check Request Values End */
                        }

                        else -> {
                            valid = false
                            errorSb.append { "event (${requestData.event});" }
                        }
                    }
                    /* Check VtRequestNodeMessage Block End */
                }
                /* Check vs Type Block End */
                else -> {
                    valid = false
                    errorSb.append { "class (${requestData::javaClass});" }
                }
            }


            //If not Valid Throw IllegalStateException with Error String
            if (!valid) {
                throw IllegalStateException(errorSb.toString())
            }

            //return true
            return true
        }
    }

}


/**
 * Class used to restrict what can be a payload for [RequestMessage.RequestMessageNodeEvent.payload]
 *
 * Must always have an [event] value at minimum
 */
@Serializable(RequestPayloadSerializer::class)
sealed class RequestPayload {
    abstract val event: String

    /**
     * Used for basic Request Payloads, like Peek
     *
     * @param event the action to be carried out
     *
     * e.g. [PayloadEvent.LIST] (*list*) will return the possible state values, [PayloadEvent.PEEK] (*peek*) will return the current state value
     */
    @Serializable
    class RequestPayloadEvent(
        override val event: String
    ) : RequestPayload()

    /**
     * Used for Request Payloads that need a token, usually Listeners
     *
     * @param event the action to be carried out
     *
     * e.g. [PayloadEvent.LISTEN] (*Listen*) will result in a State message being sent every time it changes, even when changed through the GUI or another API request.
     *
     * @param token unique id for event, same token needs to be used in subsequent related requests
     *
     * e.g. a [PayloadEvent.LISTEN] (*Listen*) Request using token '*abc123*' can be removed later by sending a [PayloadEvent.UNLISTEN] (*Unlisten*) Request with the same token
     */
    @Serializable
    data class RequestPayloadEventToken(
        override val event: String,
        /**
         * Unique ID for the listener - can be anything
         * Token sent for UnListen must be the same as original Listen Request
         */
        val token: String
    ) : RequestPayload()

    /**
     * Used for Request Payloads that need a state, e.g. Set/Push/Pop/Thumb
     *
     * @param event the action to be carried out
     *
     * e.g. [PayloadEvent.SET] (*Set*) will change the State (Current Displayed group of PNGs) to the State ID provided
     *
     * @param state unique id for a state
     *
     * i.e. the State IF from a [MessageEvent.PAYLOAD] / [MessagePayloadType.STATE_EVENTS] / [PayloadEvent.LIST] request
     */
    @Serializable
    data class RequestPayloadEventState(
        override val event: String,
        /**
         * Unique ID for the State
         *
         * i.e. the state id from a [MessageEvent.PAYLOAD] / [MessagePayloadType.STATE_EVENTS] / [PayloadEvent.LIST] request
         */
        val state: String
    ) : RequestPayload()

    fun toJsonString(): String {
        return Json.encodeToString(this)
    }

    companion object {
        /**
         * Validates Data in a Payload.
         *
         * Checks the Data Class and contents to make sure the combination is valid using [PayloadEvent] Values.
         *
         * Dynamic values like Token/State are only checked to make sure they are not Blank
         *
         * @param payloadData data object inheriting [RequestPayload]
         *
         * @return true if tests passed
         * @throws IllegalStateException if any invalid value is found. Message contains details of issues
         */
        @Throws(IllegalStateException::class)
        @JvmStatic
        fun validate(payloadData: RequestPayload): Boolean {
            val errorSb by lazy { StringBuilder().append { "Payload: " } }
            var valid = true

            when {
                (payloadData is RequestPayloadEvent) -> {
                    /* Check VtRequestPayloadEvent Block Start */
                    when (PayloadEvent.fromValue(payloadData.event)) {
                        PayloadEvent.LIST, PayloadEvent.PEEK -> {/* Valid, no more to do */
                        }

                        else -> {
                            valid = false
                            errorSb.append { "event (${payloadData.event});" }
                        }
                    }
                    /* Check VtRequestPayloadEvent Block End */
                }

                (payloadData is RequestPayloadEventToken) -> {
                    /* Check VTRequestPayloadEventToken Block Start */
                    when (PayloadEvent.fromValue(payloadData.event)) {
                        PayloadEvent.LISTEN, PayloadEvent.UNLISTEN -> {
                            /* Valid, make sure not blank */
                            if (payloadData.token.isBlank()) {
                                valid = false
                                errorSb.append { "token (is blank);" }
                            }
                        }

                        else -> {
                            valid = false
                            errorSb.append { "event (${payloadData.event});" }
                        }
                    }
                    /* Check VTRequestPayloadEventToken Block End */
                }

                (payloadData is RequestPayloadEventState) -> {
                    /* Check VTRequestPayloadEventState Block Start */
                    when (PayloadEvent.fromValue(payloadData.event)) {
                        PayloadEvent.SET, PayloadEvent.PUSH, PayloadEvent.POP, PayloadEvent.THUMB -> {
                            /* Valid, make sure not blank */
                            if (payloadData.state.isBlank()) {
                                valid = false
                                errorSb.append { "state (is blank);" }
                            }
                        }

                        else -> {
                            valid = false
                            errorSb.append { "event (${payloadData.event});" }
                        }
                    }
                    /* Check VTRequestPayloadEventState Block End */
                }

                /* Check vs Type Block End */
                else -> {
                    valid = false
                    errorSb.append { "class (${payloadData::javaClass});" }
                }

            }

            //If not Valid Throw IllegalStateException with Error String
            if (!valid) {
                throw IllegalStateException(errorSb.toString())
            }

            //return true
            return true
        }
    }
}
