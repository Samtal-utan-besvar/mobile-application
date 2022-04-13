package com.example.sub.signal

import android.util.Log
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class testData(val REASON: String, val TOKEN: String);

public class SignalWebsocketListener(val TOKEN: String) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        val v = ConnectSignalMessage(TOKEN)
        webSocket.send(Json.encodeToString(v));
        Log.d("WS-Listener: ", "Websocket Opened!")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        output("Receiving : " + text!!)
        Log.d("WS-Response: ", text!!)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        output("Receiving bytes : " + bytes!!.hex())
        Log.d("WS-Response: ", bytes!!.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket!!.close(NORMAL_CLOSURE_STATUS, null)
        output("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        output("Error : " + t.message)
    }

    companion object {
        private val NORMAL_CLOSURE_STATUS = 1000
    }

    private fun output(txt: String) {
        Log.v("WSS", txt)
    }
}