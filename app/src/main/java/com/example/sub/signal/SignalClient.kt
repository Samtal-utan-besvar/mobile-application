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
const val TOKEN1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNvbWVub2NlM0Bkb21haW4ucG9nIiwiaWF0IjoxNjQ5NzY3NDY3LCJleHAiOjE2NTAzNzIyNjd9.1W2Gn-6WDZCVMA2N3CXIGwsx-hkGNnbWCU-XVfm5Brw"
const val TOKEN2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNvbWV0aGNlM0Bkb21haW4ucG9nIiwiaWF0IjoxNjQ5NzY3NTc3LCJleHAiOjE2NTAzNzIzNzd9.XdSdDyQoNgPsglMteisgicvGQZBmnWFeVVmo4S8ZGUs"

class SignalClient {
    private var TOKEN : String = TOKEN2

    private var webSocket: WebSocket? = null
    var signalListeners = ArrayList<SignalListener>()

    init{
        Log.d("test", android.os.Build.VERSION.SDK_INT.toString())
        if (android.os.Build.VERSION.SDK_INT == 30) {
            TOKEN = TOKEN1
        } else {
            TOKEN = TOKEN2
        }
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