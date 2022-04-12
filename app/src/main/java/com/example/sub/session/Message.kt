package com.example.sub.session

abstract class Message {
    abstract var REASON: String
}

data class ConnectMessage(
    override var REASON: String = "connect",
    var TOKEN: String
) : Message()

