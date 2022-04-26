package com.example.sub.signal

import kotlinx.serialization.Serializable

/**
 * This enum class holds possible values for the 'REASON' keyword of a [Message].
 */
enum class MessageReason(val value: String) {

    CONNECT("connect"),
    CALL("call"),
    CALL_RESPONSE("callResponse"),
    ICE_CANDIDATE("ICECandidate"),
    HANG_UP("HangUp");

    override fun toString() : String {
        return value
    }

}

/**
 * Base class for messages with the 'REASON' keyword.
 */
@Serializable
abstract class Message {

    val REASON: String

    constructor(messageReason: MessageReason) {
        REASON = messageReason.value
    }
}


@Serializable
data class ConnectMessage(
    val TOKEN: String
) : Message(MessageReason.CONNECT)


@Serializable
data class CallMessage(
    val CALLER_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String,
    val SDP: String
) : Message(MessageReason.CALL)


@Serializable
data class CallResponseMessage(
    val RESPONSE: String,
    val CALLER_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String,
    val SDP: String
) : Message(MessageReason.CALL_RESPONSE)


@Serializable
data class IceCandidateMessage(
    val ORIGIN_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String,
    val CANDIDATE: String
) : Message(MessageReason.ICE_CANDIDATE)


@Serializable
data class HangupMessage(
    val CALLER_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String
) : Message(MessageReason.HANG_UP)


/**
 * The different responses that can be sent in a [CallResponseMessage].
 */
enum class CallResponse {
    ACCEPT,
    DENY;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}

