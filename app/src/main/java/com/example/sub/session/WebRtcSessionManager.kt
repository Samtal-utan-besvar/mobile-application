package com.android.webrtc.example.session

import android.content.Context
import com.example.sub.session.ServiceLocator.eglBaseContext
import com.example.sub.SignalingClient
import com.example.sub.SignalingCommand
import java.util.UUID
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.webrtc.Camera2Enumerator
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack.VIDEO_TRACK_KIND
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack

private const val VIDEO_WIDTH = 320
private const val VIDEO_HEIGHT = 240
private const val VIDEO_FPS = 30

private const val ICE_SEPARATOR = '$'

class WebRtcSessionManager(
    private val signalingClient: SignalingClient,
    private val peerConnectionUtils: PeerConnectionUtils
) {
    private val sessionManagerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val peerConnectionExecutor = Executors.newSingleThreadExecutor()

    // used to send local video track to the fragment
    private val _localVideoSinkFlow = MutableSharedFlow<VideoTrack>()
    val localVideoSinkFlow: SharedFlow<VideoTrack> = _localVideoSinkFlow

    // used to send remote video track to the sender
    private val _remoteVideoSinkFlow = MutableSharedFlow<VideoTrack>()
    val remoteVideoSinkFlow: SharedFlow<VideoTrack> = _remoteVideoSinkFlow

    // declaring video constraints and setting OfferToReceiveVideo to true
    // this step is mandatory to create valid offer and answer
    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"
            )
        )
    }

    // we need it to initialize video capturer
    private val surfaceTextureHelper = SurfaceTextureHelper.create(
        "SurfaceTextureHelperThread", eglBaseContext
    )

    private val peerConnectionFactory = peerConnectionUtils.peerConnectionFactory

    private var offer: String? = null

    private val peerConnection: PeerConnection by lazy {
        createNewPeerConnection() ?: error("peer connectrio initialization failed")
    }

    init {
        sessionManagerScope.launch {
            signalingClient.signalingCommandFlow
                .collect { commandToValue ->
                    when (commandToValue.first) {
                        SignalingCommand.OFFER -> handleOffer(commandToValue.second)
                        SignalingCommand.ANSWER -> handleAnswer(commandToValue.second)
                        SignalingCommand.ICE -> handleIce(commandToValue.second)
                        else -> Unit
                    }
                }
        }
    }

    fun onSessionScreenReady() {
        peerConnectionExecutor.execute {
            if (offer != null) {
                sendAnswer()
            } else {
                sendOffer()
            }
        }
    }

    private fun sendOffer() {
        peerConnection.createOffer(
            CallbackSdpObserver(
                onCreate = { offer ->
                    peerConnection.setLocalDescription(
                        CallbackSdpObserver(
                            onSuccess = {
                                signalingClient.sendCommand(
                                    offer.description
                                )
                            }
                        ), offer
                    )
                }
            ),
            mediaConstraints
        )
    }

    private fun sendAnswer() {
        peerConnection.setRemoteDescription(
            CallbackSdpObserver(),
            SessionDescription(SessionDescription.Type.OFFER, offer)
        )
        peerConnection.createAnswer(
            CallbackSdpObserver(
                onCreate = { answer ->
                    peerConnection.setLocalDescription(
                        CallbackSdpObserver(
                            onSuccess = {
                                signalingClient.sendCommand(
                                    answer.description
                                )
                            }
                        ),
                        answer
                    )
                }
            ), mediaConstraints
        )
    }

    private fun handleOffer(sdp: String) {
        offer = sdp
    }

    private fun handleAnswer(sdp: String) {
        peerConnection.setRemoteDescription(
            CallbackSdpObserver(), SessionDescription(SessionDescription.Type.ANSWER, sdp)
        )
    }

    private fun handleIce(iceMessage: String) {
        val iceArray = iceMessage.split(ICE_SEPARATOR)
        peerConnectionExecutor.execute {
            peerConnection.apply {
                addIceCandidate(
                    IceCandidate(
                        iceArray[0], // sdpMid
                        iceArray[1].toInt(), //sdpMLineIndex
                        iceArray[2] // sdp
                    )
                )
            }
        }
    }

    private fun createNewPeerConnection(): PeerConnection? {
        return peerConnectionFactory.createPeerConnection(
            peerConnectionUtils.rtcConfig,
            PeerConnectionObserver(
                onIceCandidateCallback = { iceCandidate ->
                    signalingClient.sendCommand(
                        "${iceCandidate.sdpMid}$ICE_SEPARATOR${iceCandidate.sdpMLineIndex}$ICE_SEPARATOR${iceCandidate.sdp}"
                    )
                },
                onTrackCallback = { rtpTransceiver ->
                    val track = rtpTransceiver?.receiver?.track() ?: return@PeerConnectionObserver
                    if (track.kind() == VIDEO_TRACK_KIND) {
                        val videoTrack = track as VideoTrack
                        sessionManagerScope.launch {
                            _remoteVideoSinkFlow.emit(videoTrack)
                        }
                    }
                }
            )
        )
    }
}