package com.example.sub.session

import android.content.Context
import com.example.sub.rtc.*
import com.example.sub.signal.*
import org.webrtc.*
import java.util.*
import kotlin.collections.ArrayList

// Class representing an existing or pending call session
class CallSession(private var signalClient: SignalClient, private var context: Context): SignalListener {

    private var rtcClient: RTCClient

    private var isHost = false
    var callerPhoneNumber: String? = null
    var targetPhoneNumber: String? = null

    private val waitingIceCandidates : Queue<IceCandidateSignalMessage> = LinkedList()
    private var hasReceivedIce = false


    private var status: CallStatus = CallStatus.CREATED
    var sessionListeners = ArrayList<SessionListener>()


    init {
        val observer = object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                if (p0 != null) {
                    val originNumber = if(isHost) callerPhoneNumber!! else targetPhoneNumber!!
                    val targetNumber = if(isHost) targetPhoneNumber!! else callerPhoneNumber!!

                    val iceCandidateSignalMessage = IceCandidateSignalMessage.fromIceCandidate(p0, originNumber, targetNumber)

                    queueIceCandidate(iceCandidateSignalMessage)
                }
            }
        }
        rtcClient = RTCClient(observer, context)
    }


    private fun setStatus(status: CallStatus) {
        if (status != this.status) {
            this.status = status
            onStatusChanged(status)
        }
    }


    private fun onStatusChanged(callStatus: CallStatus) {
        sessionListeners.forEach{ it.onSessionStatusChanged(callStatus) }

        sendWaitingIceCandidates()
    }


    fun requestCall(callerPhoneNumber: String, targetPhoneNumber: String) {
        isHost = true
        this.callerPhoneNumber = callerPhoneNumber
        this.targetPhoneNumber = targetPhoneNumber
        setStatus(CallStatus.REQUESTING)

        rtcClient.call(object : DefaultSdpObserver() {
            override fun onCreateSuccess(p0: SessionDescription?) {
                super.onCreateSuccess(p0)
                if (p0 != null) {
                    val callMessage =
                        CallSignalMessage(callerPhoneNumber, targetPhoneNumber, p0)
                    signalClient.send(callMessage)
                }
            }
        })
    }


    fun receiveCall(callSignalMessage: CallSignalMessage) {
        isHost = false
        callerPhoneNumber = callSignalMessage.CALLER_PHONE_NUMBER
        targetPhoneNumber = callSignalMessage.TARGET_PHONE_NUMBER
        setStatus(CallStatus.RECEIVING)

        val sdp = SessionDescription(SessionDescription.Type.OFFER, callSignalMessage.SDP)
        rtcClient.onRemoteSessionReceived(sdp)
    }


    fun answer() {
        rtcClient.answer(object : DefaultSdpObserver() {
            override fun onCreateSuccess(p0: SessionDescription?) {
                if (p0 != null) {
                    val callMessage =
                        CallResponseSignalMessage(CallResponse.ACCEPT, callerPhoneNumber!!, targetPhoneNumber!!, p0)
                    signalClient.send(callMessage)
                }
            }
        })
        setStatus(CallStatus.CONNECTING)
    }


    fun deny() {
        val callMessage =
            CallResponseSignalMessage(CallResponse.DENY, callerPhoneNumber!!, targetPhoneNumber!!)
        signalClient.send(callMessage)
        setStatus(CallStatus.DENIED)
    }


    fun hangUp() {
        rtcClient.endCall()
        setStatus(CallStatus.ENDED)
    }


    override fun onCallResponseMessageReceived(callResponseSignalMessage: CallResponseSignalMessage) {
        if(callResponseSignalMessage.isAllowed()){
            setStatus(CallStatus.CONNECTING)

            val sdpType = if(isHost) SessionDescription.Type.ANSWER else SessionDescription.Type.OFFER
            val sdp = callResponseSignalMessage.toSessionDescription(sdpType)
            rtcClient.onRemoteSessionReceived(sdp)

        } else{
            setStatus(CallStatus.ENDED)
        }
    }


    override fun onIceCandidateMessageReceived(iceCandidateSignalMessage: IceCandidateSignalMessage) {
        val iceCandidate = iceCandidateSignalMessage.toIceCandidate()
        rtcClient.addIceCandidate(iceCandidate)


        if (!hasReceivedIce) {
            hasReceivedIce = true
            sendWaitingIceCandidates()
        }
    }


    override fun onHangupMessageReceived(hangupSignalMessage: HangupSignalMessage) {
        setStatus(CallStatus.ENDED)
    }


    private fun queueIceCandidate(iceCandidateSignalMessage: IceCandidateSignalMessage) {
        waitingIceCandidates.add(iceCandidateSignalMessage)
        sendWaitingIceCandidates()
    }


    private fun sendWaitingIceCandidates() {
        if (canSendIce()) {
            while (!waitingIceCandidates.isEmpty()) {
                signalClient.send(waitingIceCandidates.remove())
            }
        }
    }


    private fun canSendIce() : Boolean {
        return (status == CallStatus.CONNECTING || status == CallStatus.IN_CALL) && (isHost || hasReceivedIce)
    }

}

enum class CallStatus {
    CREATED,
    RECEIVING,
    REQUESTING,
    CONNECTING,
    IN_CALL,
    DENIED,
    ENDED,
    FAILED
}