package com.example.sub.session

import com.example.sub.RTC.PeerConnectionObserver
import com.example.sub.RTC.RTCClient
import com.example.sub.signal.*
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver

// Class representing an existing or pending call session
class CallSession(signalClient: SignalClient): SignalListener {
    private var signalClient = signalClient
    private var isHost = false

    private var rtcClient: RTCClient
    private var peerConnection: PeerConnection

    private var status: CallStatus = if(isHost) CallStatus.REQUESTING else CallStatus.RECEIVING
    var statusUpdateListeners = ArrayList<(CallStatus) -> Unit>()


    init {
        var observer = PeerConnectionObserver()
        rtcClient = RTCClient(observer)
        peerConnection = rtcClient.getPeerConnection()
    }


    fun startCall(callerPhoneNumber: String, targetPhoneNumber: String) {
        isHost = true
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


}

enum class CallStatus {
    IN_CALL,
    RECEIVING,
    REQUESTING,
    CONNECTING,
    ENDED
}