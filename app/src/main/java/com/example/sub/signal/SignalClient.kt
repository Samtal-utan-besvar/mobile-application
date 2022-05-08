package com.example.sub.signal

import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val SIGNAL_URL = "ws://144.24.171.133:4000" // use local ip for devices in local network

/**
 * This class is a client that can connect with a signal server. The client uses websocket for
 * sending and receiving messages.
 */
object SignalClient {

    private var token : String = ""

    private var webSocket: WebSocket? = null
    var signalListeners = ArrayList<SignalMessageListener>()


    /**
     * Requests and establishes a connection with the signal server.
     */
    fun connect(token: String) {
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
    private class SignalingWebSocketListener : WebSocketListener() {


        /**
         * Gets called when the websocket connection is established.
         */
        override fun onOpen(webSocket: WebSocket, response: Response) {
            send(ConnectMessage(token))
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
                when (reason) {
                    MessageReason.CALL.value -> handleCallMessage(text)
                    MessageReason.CALL_RESPONSE.value -> handleCallResponseCommand(text)
                    MessageReason.ICE_CANDIDATE.value -> handleICECandidateCommand(text)
                    MessageReason.HANG_UP.value -> handleHangUpCommand(text)

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
            throw t
        }
    }


    /**
     * Closes the websocket connection to the signal server.
     */
    fun close() {
        webSocket?.close(1000, "SignalClient close called")
    }



    //region Handle functions

    /**
     * Handles incoming messages of type [HangupMessage].
     */
    private fun handleHangUpCommand(text: String) {
        val hangupMessage = Json.decodeFromString(HangupMessage.serializer(), text)
        signalListeners.forEach {
            it.onHangupMessageReceived(hangupMessage)
        }
    }


    /**
     * Handles incoming messages of type [IceCandidateMessage].
     */
    private fun handleICECandidateCommand(text: String) {
        val iceCandidateMessage = Json.decodeFromString(IceCandidateMessage.serializer(), text)
        signalListeners.forEach {
            it.onIceCandidateMessageReceived(iceCandidateMessage)
        }
    }


    /**
     * Handles incoming messages of type [CallResponseMessage].
     */
    private fun handleCallResponseCommand(text: String) {
        val callResponseMessage = Json.decodeFromString(CallResponseMessage.serializer(), text)
        signalListeners.forEach {
            it.onCallResponseMessageReceived(callResponseMessage)
        }
    }


    /**
     * Handles incoming messages of type [CallMessage].
     */
    private fun handleCallMessage(text: String) {
        val callMessage = Json.decodeFromString(CallMessage.serializer(), text)
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
     * Sends a [ConnectMessage] to the signal server.
     */
    fun send(message: ConnectMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }


    /**
     * Sends a [CallMessage] to the signal server.
     */
    fun send(message: CallMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }


    /**
     * Sends a [CallResponseMessage] to the signal server.
     */
    fun send(message: CallResponseMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }


    /**
     * Sends a [IceCandidateMessage] to the signal server.
     */
    fun send(message: IceCandidateMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }


    /**
     * Sends a [HangupMessage] to the signal server.
     */
    fun send(message: HangupMessage) {
        val msg = Json.encodeToString(message)
        send(msg)
    }

    //endregion


}