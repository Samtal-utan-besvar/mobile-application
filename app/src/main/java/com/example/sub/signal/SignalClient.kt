package com.example.sub.signal

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val SIGNAL_URL = "ws://144.24.171.133:4000" // use local ip for devices in local network
// pixel 5: utan 5, Brw
const val TOKEN1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNvbXNkZUBkbWFhaW4ucG9nIiwiaWF0IjoxNjUwNDQ1MjcyLCJleHAiOjE2NTEwNTAwNzJ9.zMfKiU2Xxy1Wde-c1PVSe-NNJagoIRMIGeMuV8e3geE"
const val TOKEN2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNvbXNkZUBkbWFhaWRkbi5wb2ciLCJpYXQiOjE2NTA0NDUyOTksImV4cCI6MTY1MTA1MDA5OX0.WUhVjyj-Is_2FWk3LnWjAyn0tcYsBAARgPBDbc6-1oM"

class SignalClient {
    private var TOKEN : String = TOKEN2

    private var webSocket: WebSocket? = null
    var signalListeners = ArrayList<SignalListener>()

    init{
        Log.d("test", android.os.Build.VERSION.SDK_INT.toString())
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
            send(ConnectSignalMessage(TOKEN))
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
                    reason.equals(ReasonCommand.CALLRESPONSE.toString(), true) ->
                        handleCallResponseCommand(text)
                    reason.equals(ReasonCommand.ICECANDIDATE.toString(), true) ->
                        handleICECandidateCommand(text)
                    reason.equals(ReasonCommand.HANGUP.toString(), true) ->
                        handleHangUpCommand(text)
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

    fun send(message: CallResponseSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }

    fun send(message: IceCandidateSignalMessage) {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                // This method will be executed once the timer is over
                val msg = Json.encodeToString(message)
                send(msg)
            },
            10000 // value in milliseconds
        )

    }

    fun send(message: HangupSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }



    enum class ReasonCommand {
        CALL,
        CALLRESPONSE,
        ICECANDIDATE,
        HANGUP
    }

}