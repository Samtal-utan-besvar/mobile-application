package com.example.sub.signal

import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val SIGNAL_URL = "ws://144.24.171.133:4000" // use local ip for devices in local network

class SignalClient {
    private var TOKEN : String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNvbWV0aGNlM0Bkb21haW4ucG9nIiwiaWF0IjoxNjQ5NzY3NTc3LCJleHAiOjE2NTAzNzIzNzd9.XdSdDyQoNgPsglMteisgicvGQZBmnWFeVVmo4S8ZGUs"

    private var webSocket: WebSocket? = null
    var signalListeners = ArrayList<SignalListener>()

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
            .url(SIGNAL_URL) // 'ws'
            .build()
        val wsListener = SignalingWebSocketListener()
        webSocket = client.newWebSocket(request, wsListener) // this provide to make 'Open ws connection'
    }

    private inner class SignalingWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            val v = ConnectSignalMessage(TOKEN)
            webSocket.send(Json.encodeToString(v));
            Log.d("WS-Listener: ", "Websocket Opened!")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val jsonObject = JSONObject(text)

            if (jsonObject.has("REASON")) {
                val reason = jsonObject.get("REASON").toString().uppercase()
                Log.d("reason", reason)

                when {
                    reason.startsWith(ReasonCommand.CALL.toString(), true) ->
                        handleCallMessage(text)
                    reason.startsWith(ReasonCommand.CALLRESPONSE.toString(), true) ->
                        handleCallResponseCommand(text)
                    reason.startsWith(ReasonCommand.ICECANDIDATE.toString(), true) ->
                        handleICECandidateCommand(text)
                    reason.startsWith(ReasonCommand.HANGUP.toString(), true) ->
                        handleHangUpCommand(text)
                }

            } else if (jsonObject.has("RESPONSE")) {
                val response = jsonObject.get("RESPONSE").toString()
                Log.d("response", response)
            }



            /*
            when {
                text.startsWith(SignalingCommand.STATE.toString(), true) ->
                    handleStateMessage(text)
                text.startsWith(SignalingCommand.OFFER.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.OFFER, text)
                text.startsWith(SignalingCommand.ANSWER.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.ANSWER, text)
                text.startsWith(SignalingCommand.ICE.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.ICE, text)
            }

             */
        }
    }

    private fun handleHangUpCommand(text: String) {
        val hangupMessage = Json.decodeFromString(HangupSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onHangupMessageReceived(hangupMessage)
        }
    }

    private fun handleICECandidateCommand(text: String) {
        val iceCandidateMessage = Json.decodeFromString(IceCandidateSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onIceCandidateMessageReceived(iceCandidateMessage)
        }
    }

    private fun handleCallResponseCommand(text: String) {
        val callResponseMessage = Json.decodeFromString(CallResponseSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onCallResponseMessageReceived(callResponseMessage)
        }
    }

    private fun handleCallMessage(text: String) {
        val callMessage = Json.decodeFromString(CallSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onCallMessageReceived(callMessage)
        }
    }

    fun sendCallMessage(message: CallSignalMessage) {
        val msg = Json.encodeToString(message)
        webSocket?.send(msg)
    }

    fun sendCallResponseMessage(message: CallResponseSignalMessage) {
        val msg = Json.encodeToString(message)
        webSocket?.send(msg)
    }

    enum class ReasonCommand {
        CALL,
        CALLRESPONSE,
        ICECANDIDATE,
        HANGUP
    }

}