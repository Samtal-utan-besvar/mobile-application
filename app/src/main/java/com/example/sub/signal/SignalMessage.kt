package com.example.sub.signal

import kotlinx.serialization.*
import org.webrtc.SessionDescription

@Serializable
abstract class SignalMessage(var REASON: String) {
    var reason: String = REASON
}

@Serializable
data class ConnectSignalMessage(
    var TOKEN: String
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
    var CALLER_PHONE_NUMBER: String,
    var TARGET_PHONE_NUMBER: String,
    var SDP: String
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
    var RESPONSE: String,
    var CALLER_PHONE_NUMBER: String,
    var TARGET_PHONE_NUMBER: String,
    var SDP: String
) : SignalMessage("callResponse") {
    fun isAllowed(): Boolean{
        return (RESPONSE === CallResponse.ALLOW.toString())
    }
}

enum class CallResponse {
    ALLOW,
    DENY;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}


@Serializable
data class IceCandidateSignalMessage(
    var CALLER_PHONE_NUMBER: String,
    var TARGET_PHONE_NUMBER: String,
    var CANDIDATE: String
) : SignalMessage("ICECandidate")


@Serializable
data class HangupSignalMessage(
    var CALLER_PHONE_NUMBER: String,
    var TARGET_PHONE_NUMBER: String
) : SignalMessage("HangUp")

