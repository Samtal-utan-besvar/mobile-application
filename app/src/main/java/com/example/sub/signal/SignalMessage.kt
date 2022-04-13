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
) : SignalMessage("call")


@Serializable
data class CallResponseSignalMessage(
    var RESPONSE: String,
    var CALLER_PHONE_NUMBER: String,
    var SDP: String,
    var TARGET_PHONE_NUMBER: String
) : SignalMessage("callResponse")

