package com.example.sub.rtc

import android.util.Log
import org.webrtc.*

private const val TAG = "PeerConnectionObserver"

/**
 * Implement this in a class to be able to get notified of changes in a [PeerConnection].
 */
interface PeerConnectionObserver: PeerConnection.Observer {

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d(TAG, "signal change " + p0?.toString())
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "ice connection change " + p0?.toString())
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d(TAG, "ice connection receiving change " + p0.toString())
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.d(TAG, "ice gathering change " + p0?.toString())
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.d(TAG, "ice candidate " + p0?.toString())
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.d(TAG, "ice candidates removed " + p0?.toString())
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.d(TAG, "add stream " + p0?.toString())
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.d(TAG, "remove stream " + p0?.toString())
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d(TAG, "data channel " + p0?.toString())
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "renegotiation needed")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.d(TAG, "add track" + p0?.toString())
    }

    fun onStringMessage(p0: String) {
        Log.d(TAG, "string message " + p0)
    }

    fun onBytesMessage(p0: ByteArray) {
        Log.d(TAG, "bytes message " + p0.joinToString(", ") { "%02x".format(it) })
    }
}