package com.example.sub.signal

import kotlinx.serialization.*

@Serializable
abstract class SignalMessage(var REASON: String) {
    var reason: String = REASON
}

@Serializable
data class ConnectSignalMessage(
    var TOKEN: String
) : SignalMessage("connect")


@Serializable
data class CallSignalMessage(
    var CALLER_PHONE_NUMBER: String,
    var TARGET_PHONE_NUMBER: String,
    var SDP: String
) : SignalMessage("call") {
    fun toResponse(answer: Boolean, sdp: String = "-"): CallResponseSignalMessage{
        val response = if(answer) "allow" else "deny"
        return CallResponseSignalMessage( response, CALLER_PHONE_NUMBER, TARGET_PHONE_NUMBER, sdp)
    }
}


@Serializable
data class CallResponseSignalMessage(
    var RESPONSE: String,
    var CALLER_PHONE_NUMBER: String,
    var TARGET_PHONE_NUMBER: String,
    var SDP: String
) : SignalMessage("callResponse") {
    fun isAllowed(): Boolean{
        return (RESPONSE === "allow")
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

