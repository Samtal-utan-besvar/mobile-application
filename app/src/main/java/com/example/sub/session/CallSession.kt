package com.example.sub.session

import android.content.Context
import android.util.Log
import com.example.sub.RTC.PeerConnectionObserver
import com.example.sub.RTC.RTCClient
import com.example.sub.signal.*
import org.webrtc.*

// Class representing an existing or pending call session
class CallSession(signalClient: SignalClient, context: Context): SignalListener {
    private var signalClient = signalClient
    private var isHost = false

    private var rtcClient: RTCClient
    private var peerConnection: PeerConnection

    private var status: CallStatus = if(isHost) CallStatus.REQUESTING else CallStatus.RECEIVING
    var statusUpdateListeners = ArrayList<(CallStatus) -> Unit>()

    val pcConstraints = object : MediaConstraints() {
        init {
            optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        }
    }


    init {
        var observer = PeerConnectionObserver()
        rtcClient = RTCClient(observer, context)
        peerConnection = rtcClient.getPeerConnection()
    }


    fun startCall(callerPhoneNumber: String, targetPhoneNumber: String) {
        isHost = true

        peerConnection.createOffer(object : DefaultSdpObserver() {
            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.d("offer", p0.toString())
            }
        }, pcConstraints)
    }


    fun answerCall(callSignalMessage: CallSignalMessage) {
        isHost = false
    }


    private fun setStatus(status: CallStatus) {
        this.status = status
        statusUpdateListeners.forEach { it.invoke(status) }
    }

    override fun onCallResponseMessageReceived(callResponseSignalMessage: CallResponseSignalMessage) {
        if(!isHost){
            if(callResponseSignalMessage.isAllowed()){
                setStatus(CallStatus.CONNECTING)



            } else{
                setStatus(CallStatus.ENDED)
            }
        }
    }

    override fun onIceCandidateMessageReceived(iceCandidateSignalMessage: IceCandidateSignalMessage) {
        TODO("Not yet implemented")
    }

    override fun onHangupMessageReceived(hangupSignalMessage: HangupSignalMessage) {
        TODO("Not yet implemented")
    }

    open inner class DefaultSdpObserver : SdpObserver {

        override fun onCreateSuccess(p0: SessionDescription?) {

        }

        override fun onCreateFailure(p0: String?) {
            Log.d("CallSession","failed to create offer:$p0")
        }

        override fun onSetFailure(p0: String?) {
            Log.d("CallSession","set failure:$p0")
        }

        override fun onSetSuccess() {
            Log.d("CallSession","set success")
        }

    }

}

enum class CallStatus {
    IN_CALL,
    RECEIVING,
    REQUESTING,
    CONNECTING,
    ENDED
}