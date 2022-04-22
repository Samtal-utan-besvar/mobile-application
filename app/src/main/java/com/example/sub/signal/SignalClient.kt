package com.example.sub.signal

import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val SIGNAL_URL = "ws://144.24.171.133:4000" // use local ip for devices in local network

const val TOKEN1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNvbXNkZUBkbWFhaW4ucG9nIiwiaWF0IjoxNjUwNDQ1MjcyLCJleHAiOjE2NTEwNTAwNzJ9.zMfKiU2Xxy1Wde-c1PVSe-NNJagoIRMIGeMuV8e3geE"
const val TOKEN2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNvbXNkZUBkbWFhaWRkbi5wb2ciLCJpYXQiOjE2NTA0NDUyOTksImV4cCI6MTY1MTA1MDA5OX0.WUhVjyj-Is_2FWk3LnWjAyn0tcYsBAARgPBDbc6-1oM"


/**
 * This class is a client that can connect with a signal server. The client uses websocket for
 * sending and receiving messages.
 */
class SignalClient {

    private var token : String = TOKEN2

    private var webSocket: WebSocket? = null
    var signalListeners = ArrayList<SignalListener>()

    init{
        Log.d("test", android.os.Build.VERSION.SDK_INT.toString())

        // Temporary solution.
        val TOKEN = if (android.os.Build.VERSION.SDK_INT == 30) TOKEN1 else TOKEN2
        connect(TOKEN)
    }


    /**
     * Requests and establishes a connection with the signal server.
     */
    fun connect(token: String){
        this.token = token

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


    /**
     * This is a class utilized for listening to changes of the websocket.
     */
    private inner class SignalingWebSocketListener : WebSocketListener() {


        /**
         * Gets called when the websocket connection is established.
         */
        override fun onOpen(webSocket: WebSocket, response: Response) {
            send(ConnectSignalMessage(token))
            Log.d("WS-Listener: ", "Websocket Opened!")
        }


        /**
         * Gets called when a message is received from the signal server and delegates the message
         * to suitable handler functions.
         */
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("Signal-receive", text)
            val jsonObject = JSONObject(text)

            if (jsonObject.has("REASON")) {
                // Handle messages with the REASON keyword.
                val reason = jsonObject.get("REASON").toString()

                // Delegate messages to correct handlers.
                when {
                    reason.equals(ReasonCommand.CALL.toString(), true) ->
                        handleCallMessage(text)
                    reason.equals(ReasonCommand.CALLRESPONSE.toString(), true) ->
                        handleCallResponseCommand(text)
                    reason.equals(ReasonCommand.ICECANDIDATE.toString(), true) ->
                        handleICECandidateCommand(text)
                    reason.equals(ReasonCommand.HANGUP.toString(), true) ->
                        handleHangUpCommand(text)

                    // Log error if message could not be matched with any of the handlers.
                    else -> Log.e("Signal-receive", "reason: $reason has no handler")
                }

            } else if (jsonObject.has("RESPONSE")) {
                // Handle messages with the RESPONSE keyword.
                val response = jsonObject.get("RESPONSE").toString()
                // Not implemented yet
            } else {
                // No handler exists for this type of message. Log error.
                Log.e("Signal-receive", "Unknown message: $text")
            }
        }


        /**
         * Gets called when the websocket connection has closed
         */
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d("Signal-closed", reason)
        }


        /**
         * Gets called when an error occurs with the websocket connection, sent messages or
         * received messages.
         */
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            val msg = response?.message ?: t.message ?: ""
            Log.d("Signal-fail", msg)
        }
    }



    //region Handle functions

    /**
     * Handles incoming messages of type [HangupSignalMessage].
     */
    private fun handleHangUpCommand(text: String) {
        val hangupMessage = Json.decodeFromString(HangupSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onHangupMessageReceived(hangupMessage)
        }
    }


    /**
     * Handles incoming messages of type [IceCandidateSignalMessage].
     */
    private fun handleICECandidateCommand(text: String) {
        val iceCandidateMessage = Json.decodeFromString(IceCandidateSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onIceCandidateMessageReceived(iceCandidateMessage)
        }
    }


    /**
     * Handles incoming messages of type [CallResponseSignalMessage].
     */
    private fun handleCallResponseCommand(text: String) {
        val callResponseMessage = Json.decodeFromString(CallResponseSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onCallResponseMessageReceived(callResponseMessage)
        }
    }


    /**
     * Handles incoming messages of type [CallSignalMessage].
     */
    private fun handleCallMessage(text: String) {
        val callMessage = Json.decodeFromString(CallSignalMessage.serializer(), text)
        signalListeners.forEach {
            it.onCallMessageReceived(callMessage)
        }
    }

    //endregion



    //region Send functions

    /**
     * Sends a [String] to the signal server.
     */
    private fun send(message: String) {
        Log.d("Signal-send", message)
        webSocket?.send(message)
    }


    /**
     * Sends a [ConnectSignalMessage] to the signal server.
     */
    fun send(message: ConnectSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }


    /**
     * Sends a [CallSignalMessage] to the signal server.
     */
    fun send(message: CallSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }


    /**
     * Sends a [CallResponseSignalMessage] to the signal server.
     */
    fun send(message: CallResponseSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }


    /**
     * Sends a [IceCandidateSignalMessage] to the signal server.
     */
    fun send(message: IceCandidateSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }


    /**
     * Sends a [HangupSignalMessage] to the signal server.
     */
    fun send(message: HangupSignalMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }

    //endregion


    /**
     * Enum for reasons of each [SignalMessage].
     */
    enum class ReasonCommand {
        CALL,
        CALLRESPONSE,
        ICECANDIDATE,
        HANGUP
    }

}