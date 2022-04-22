package com.example.sub.signal

import kotlinx.serialization.*


@Serializable
abstract class SignalMessage(private val REASON: String)


@Serializable
data class ConnectSignalMessage(
    val TOKEN: String
) : SignalMessage("connect")


@Serializable
data class CallSignalMessage(
    val CALLER_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String,
    val SDP: String
) : SignalMessage("call")


@Serializable
data class CallResponseSignalMessage(
    val RESPONSE: String,
    val CALLER_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String,
    val SDP: String
) : SignalMessage("callResponse")


@Serializable
data class IceCandidateSignalMessage(
    val ORIGIN_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String,
    val CANDIDATE: String
) : SignalMessage("ICECandidate")


@Serializable
data class HangupSignalMessage(
    val CALLER_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String
) : SignalMessage("HangUp")



enum class CallResponse {
    ACCEPT,
    DENY;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}

