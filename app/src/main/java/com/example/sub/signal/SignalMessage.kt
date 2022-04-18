package com.example.sub.signal

import kotlinx.serialization.*
import org.webrtc.SessionDescription

@Serializable
abstract class SignalMessage(private val REASON: String) {}


@Serializable
data class ConnectSignalMessage(
    val TOKEN: String
) : SignalMessage("connect")


fun CallSignalMessage(
    CALLER_PHONE_NUMBER: String,
    TARGET_PHONE_NUMBER: String,
    sdp: SessionDescription): CallSignalMessage =
    CallSignalMessage(
        CALLER_PHONE_NUMBER,
        TARGET_PHONE_NUMBER,
        sdp.description)

@Serializable
data class CallSignalMessage(
    val CALLER_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String,
    val SDP: String
) : SignalMessage("call") {

    fun toResponse(callResponse: CallResponse, sdp: SessionDescription? = null): CallResponseSignalMessage {
        return CallResponseSignalMessage(callResponse, CALLER_PHONE_NUMBER, TARGET_PHONE_NUMBER, sdp)
    }

}


fun CallResponseSignalMessage(
    callResponse: CallResponse,
    CALLER_PHONE_NUMBER: String,
    TARGET_PHONE_NUMBER: String,
    sdp: SessionDescription? = null): CallResponseSignalMessage =
    CallResponseSignalMessage(
        callResponse.toString(),
        CALLER_PHONE_NUMBER,
        TARGET_PHONE_NUMBER,
        if (sdp == null) "rick roll" else sdp.description)

@Serializable
data class CallResponseSignalMessage(
    val RESPONSE: String,
    val CALLER_PHONE_NUMBER: String,
    val TARGET_PHONE_NUMBER: String,
    val SDP: String
) : SignalMessage("callResponse") {
    fun isAllowed(): Boolean{
        return (RESPONSE === CallResponse.ACCEPT.toString())
    }
}

enum class CallResponse {
    ACCEPT,
    DENY;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}


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

