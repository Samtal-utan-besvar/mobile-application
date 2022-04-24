package com.example.sub.rtc

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

/**
 * A client that is used to set up a webRTC connection between two peers.
 */
class RTCClient(observer: PeerConnectionObserver, context: Context) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    private var observer: PeerConnection.Observer
    val TAG = "RTCClient"

    private var remoteSessionDescription: SessionDescription? = null

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints())}
    private val peerConnection by lazy { buildPeerConnection() }

    private var localAudioTrack : AudioTrack? = null


    // List of stun and turn servers that can be used for the peer to peer connection.
    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer(),
        //PeerConnection.IceServer.builder("stun:141.144.249.42:3478")
        //            .createIceServer(),
        PeerConnection.IceServer.builder("turn:141.144.249.42:3478").setUsername("test")
            .setPassword("test123").createIceServer()
    )


    // Adds necessary functionality to the observer and initializes the PeerConnectionFactory
    init {
        this.observer = object : PeerConnection.Observer by observer {

            override fun onDataChannel(p0: DataChannel?) {
                Log.d("RTCClient-channel", p0.toString())
                observer.onDataChannel(p0)
                channel = p0
                p0?.registerObserver(DefaultDataChannelObserver(p0))
            }
        }
        initPeerConnectionFactory(context)
    }


    /**
     * Initializes the [PeerConnectionFactory] with the given [context].
     */
    private fun initPeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }


    /**
     * Builds and applies options to the [PeerConnectionFactory].
     */
    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setOptions(PeerConnectionFactory.Options().apply {
                //disableEncryption = true
                //disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }


    /**
     * Builds and returns a [PeerConnection].
     */
    private fun buildPeerConnection(): PeerConnection {
        return buildPeerConnectionFactory().createPeerConnection(
            iceServer,
            observer
        )!!
    }


    /**
     * Adds audio to the webRTC connection. By default the sound is automatically
     * played in the speakers.
     */
    private fun initAudio() {
        localAudioTrack = peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID, audioSource)
        peerConnection.addTrack(localAudioTrack)
    }


    /**
     * Extension function to the [PeerConnection] that is used when the call is
     * started locally.
     */
    private fun PeerConnection.call(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        makeDataChannel()
        initAudio()
        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(sdpObserver, desc)
                Log.d("local-desc", desc!!.description)
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }


    /**
     * Extension function to the [PeerConnection] that is used when the call is
     * started by a remote peer and is accepted.
     */
    private fun PeerConnection.answer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        initAudio()
        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(sdpObserver, desc)
                Log.d("local-desc", desc!!.description)
                sdpObserver.onCreateSuccess(desc)
            }
        }, constraints)
    }


    /**
     * Sets up webRTC as the caller. The [sdpObserver] should be implemented to send the
     * [SessionDescription] created as an offer to the opposing peer.
     */
    fun call(sdpObserver: SdpObserver) = peerConnection.call(sdpObserver)


    /**
     * Sets up webRTC as the receiver.The [sdpObserver] should be implemented to send the
     * [SessionDescription] created as an answer to the opposing peer.
     */
    fun answer(sdpObserver: SdpObserver) = peerConnection.answer(sdpObserver)


    /**
     * Sets up webRTC with the given [sessionDescription] as the remote description.
     */
    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        remoteSessionDescription = sessionDescription

        peerConnection.setRemoteDescription(DefaultSdpObserver(), sessionDescription)
    }


    /**
     * Adds the given [iceCandidate] to the possible candidates for the webRTC connection.
     */
    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection.addIceCandidate(iceCandidate)
    }


    /**
     * Closes the webRTC connection.
     */
    fun endCall() {
        peerConnection.close()
    }


    // Testing stuff. Might not be needed in final product.
    //region Data channel

    var channel: DataChannel? = null


    /**
     * Creates and adds a data channel to the webRTC connection that can be used
     * for sending string or binary data.
     */
    private fun makeDataChannel() {
        val init = DataChannel.Init()
        channel = peerConnection.createDataChannel("123", init)
        channel!!.registerObserver(DefaultDataChannelObserver(channel!!))
    }


    /**
     * Sends the given [message] to the opposing peer through
     * the data channel of the webRTC connection.
     */
    fun sendMessage(message: String) {
        val sendJSON = JSONObject()
        sendJSON.put("msg", message)
        val buf = ByteBuffer.wrap(sendJSON.toString().toByteArray(UTF_8))
        channel?.send(DataChannel.Buffer(buf, false))
    }


    /**
     * Observer for the [DataChannel] of the webRTC connection. Handles the receiving of messages
     * and state changes.
     */
    open inner class DefaultDataChannelObserver(val channel: DataChannel) : DataChannel.Observer {

        // Handles received messages.
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
                sendMessage("rick roll")
            } else {
                Log.d("RTCClient-state","Chat ended.")
            }
        }
    }

    //endregion

}