package com.example.sub.rtc

import android.content.Context
import android.util.Log
import org.webrtc.*

class RTCClient(observer: PeerConnectionObserver, context: Context) {

    private var observer = observer
    val TAG = "RTCClient"

    private var remoteSessionDescription: SessionDescription? = null

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints())}
    private val peerConnection by lazy { buildPeerConnection() }


    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )


    init {
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
                disableEncryption = true
                disableNetworkMonitor = true
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

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(sdpObserver, desc)
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

}