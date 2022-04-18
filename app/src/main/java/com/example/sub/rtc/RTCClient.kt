package com.example.sub.rtc

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

class RTCClient(observer: PeerConnectionObserver, context: Context) {

    private var observer: PeerConnection.Observer
    val TAG = "RTCClient"

    private var remoteSessionDescription: SessionDescription? = null

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints())}
    private val peerConnection by lazy { buildPeerConnection() }


    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer(),
        //PeerConnection.IceServer.builder("turn:141.144.249.42:4000")
        //    .createIceServer()
    )


    init {
        this.observer = object : PeerConnection.Observer by observer {

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                Log.d("RTCClient-ice", p0.toString())
            }

            override fun onDataChannel(p0: DataChannel?) {
                Log.d("RTCClient-channel", p0.toString())
                this.onDataChannel(p0)
                channel = p0
                p0?.registerObserver(DefaultDataChannelObserver(p0))
            }
        }
        initPeerConnectionFactory(context)
    }


    private fun initPeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }


    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setOptions(PeerConnectionFactory.Options().apply {
                //disableEncryption = true
                //disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }


    private fun buildPeerConnection(): PeerConnection {
        return buildPeerConnectionFactory().createPeerConnection(
            iceServer,
            observer
        )!!
    }


    private fun PeerConnection.call(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        makeDataChannel()
        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(sdpObserver, desc)
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }


    private fun PeerConnection.answer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(sdpObserver, desc)
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }


    fun call(sdpObserver: SdpObserver) = peerConnection?.call(sdpObserver)

    fun answer(sdpObserver: SdpObserver) = peerConnection?.answer(sdpObserver)


    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        remoteSessionDescription = sessionDescription
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onSetSuccess() {
                Log.e(TAG, "onSetSuccessRemoteSession")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.e(TAG, "onCreateSuccessRemoteSession: Description $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure")
            }
        }, sessionDescription)
    }


    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection.addIceCandidate(iceCandidate)
    }


    fun endCall() {
        peerConnection.close()
    }


    // Testing stuff

    var channel: DataChannel? = null

    fun makeDataChannel() {
        val init = DataChannel.Init()
        channel = peerConnection.createDataChannel("123", init)
        channel!!.registerObserver(DefaultDataChannelObserver(channel!!))
    }

    fun sendMessage(message: String) {
        val sendJSON = JSONObject()
        sendJSON.put("msg", message)
        val buf = ByteBuffer.wrap(sendJSON.toString().toByteArray(UTF_8))
        channel?.send(DataChannel.Buffer(buf, false))
    }

    open inner class DefaultDataChannelObserver(val channel: DataChannel) : DataChannel.Observer {


        //TODO I'm not sure if this would handle really long messages
        override fun onMessage(p0: DataChannel.Buffer?) {
            val buf = p0?.data
            if (buf != null) {
                val byteArray = ByteArray(buf.remaining())
                buf.get(byteArray)
                val received = String(byteArray, UTF_8)
                try {
                    val message = JSONObject(received).getString("msg")
                    Log.d("RTCClient-receive", message)
                } catch (e: JSONException) {
                    Log.d("RTCClient-receive", "error")
                }


            }
        }

        override fun onBufferedAmountChange(p0: Long) {
            Log.d("RTCClient-buffered","channel buffered amount change:{$p0}")
        }

        override fun onStateChange() {
            Log.d("RTCClient-state","Channel state changed:${channel.state()?.name}}")
            if (channel.state() == DataChannel.State.OPEN) {
                Log.d("RTCClient-state","Chat established.")
            } else {
                Log.d("RTCClient-state","Chat ended.")
            }
        }
    }

}