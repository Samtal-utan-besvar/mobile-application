package com.example.sub.transcription

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TRANSCRIPT_URL = "ws://129.151.209.72:6000" // use local ip for devices in local network
// pixel 5: utan 5, Brw

class SignalClient {

    private var webSocket: WebSocket? = null
    var signalListeners = ArrayList<TranscriptionListener>()

    init{
        connect()
    }

    fun connect(){
        // wss test
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            //.sslSocketFactory()
            .build()
        val request = Request.Builder()
            .url(TRANSCRIPT_URL) // 'ws'
            .build()
        val wsListener = SignalingWebSocketListener()
        webSocket = client.newWebSocket(request, wsListener) // this provide to make 'Open ws connection'
    }

    private inner class SignalingWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            //send(ConnectSignalMessage())
            Log.d("WS-Listener: ", "Websocket Opened!")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("Signal-receive", text)
            val jsonObject = JSONObject(text)

            if (jsonObject.has("REASON")) {
                val reason = jsonObject.get("REASON").toString()
                Log.d("reason", reason)

                when {
                    reason.equals(ReasonCommand.CALL.toString(), true) ->
                        handleCallMessage(text)
                    else -> Log.e("Signal-receive", "$reason did not match")
                }

            } else if (jsonObject.has("RESPONSE")) {
                val response = jsonObject.get("RESPONSE").toString()
                Log.d("response", response)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d("Signal-closed", reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            val msg = response?.message ?: t.message ?: ""
            Log.d("Signal-fail", msg)
        }
    }

    private fun handleCallMessage(text: String) {
        val callMessage = Json.decodeFromString(CallSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onCallMessageReceived(callMessage)
        }
    }


    private fun send(message: String) {
        Log.d("Signal-send", message)
        webSocket?.send(message)
    }

    fun send(message: ConnectSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }

    fun send(message: CallSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }

    enum class ReasonCommand {
        CALL
    }

}