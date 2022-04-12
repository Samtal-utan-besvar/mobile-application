package com.example.sub.session

import kotlinx.serialization.Serializable

@Serializable
abstract class Message(var REASON: String) {
    var reason: String = REASON
}

@Serializable
data class ConnectMessage(
    var TOKEN: String
) : Message("connect")


@Serializable
data class CallMessage(
    var CALLER_PHONE_NUMBER: String,
    var TARGET_PHONE_NUMBER: String,
    var SDP: String
) : Message("call")

