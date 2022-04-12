package com.example.sub

import android.util.Log
import com.example.sub.session.CallMessage
import com.example.sub.session.ConnectMessage
import com.example.sub.session.Message
import com.example.sub.session.SignalWebsocketListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

private const val SIGNALING_URL = "ws://144.24.171.133:4000" // use local ip for devices in local network

class SignalingClient {
    private var TOKEN : String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNvbWV0aGNlM0Bkb21haW4ucG9nIiwiaWF0IjoxNjQ5NzY3NTc3LCJleHAiOjE2NTAzNzIzNzd9.XdSdDyQoNgPsglMteisgicvGQZBmnWFeVVmo4S8ZGUs"

    private var webSocket: WebSocket? = null

    fun connect(){
        // wss test
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            //.sslSocketFactory()
            .build()
        val request = Request.Builder()
            .url(SIGNALING_URL) // 'ws'
            .build()
        val wsListener = SignalWebsocketListener(TOKEN)
        webSocket = client.newWebSocket(request, wsListener) // this provide to make 'Open ws connection'
    }

    fun sendCallMessage(message: CallMessage) {
        val msg = Json.encodeToString(message)
        webSocket?.send(msg)
    }

}