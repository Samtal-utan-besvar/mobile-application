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

class TranscriptionClient {

    private var webSocket: WebSocket? = null

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
        val wsListener = TranscribingWebSocketListener()
        webSocket = client.newWebSocket(request, wsListener) // this provide to make 'Open ws connection'
    }

    private inner class TranscribingWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            //send(ConnectSignalMessage())
            Log.d("WS-Listener: ", "Websocket Opened!")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("Signal-receive", text)

            if (text != "") {
                Log.d("Answer", text)

            } else if (text == "") {
                Log.d("Answer", "Empty answer")
            }
            else{
                Log.d("Answer", "Other answer")
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Log.d("Transcription-closed", reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            val msg = response?.message ?: t.message ?: ""
            Log.d("Transcription-fail", msg)
        }
    }

    fun sendAnswer(id: Int, ownertype: String) {
        Log.d("Transcription-send", ownertype)
        val message = JSONObject()
        message.put("Reason", "answer")
        message.put("Id", id)
        message.put("Data", ownertype)
        webSocket?.send(Json.encodeToString(message))
    }

    fun sendSound(id: Int, sound: String) {
        Log.d("Transcription-send", "sound")

        //convert sound to string here()

        val message = JSONObject()
        message.put("Reason", "answer")
        message.put("Id", id)
        message.put("Data", sound)
        webSocket?.send(Json.encodeToString(message))
    }

}