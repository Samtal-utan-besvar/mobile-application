package com.example.sub.session

import android.content.Context
import com.example.sub.rtc.*
import com.example.sub.signal.*
import org.json.JSONObject
import org.webrtc.*

// Class representing an existing or pending call session
class CallSession(private var signalClient: SignalClient, private var context: Context): SignalListener {

    private var rtcClient: RTCClient


    private var isHost = false
    var callerPhoneNumber: String? = null
    var targetPhoneNumber: String? = null


    private var status: CallStatus = CallStatus.CREATED
    var statusUpdateListeners = ArrayList<(CallStatus) -> Unit>()


    init {
        val observer = object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                if (p0 != null) {
                    val jsonObject = JSONObject()
                    jsonObject.put("sdpMid", p0.sdpMid)
                    jsonObject.put("sdpMLineIndex", p0.sdpMLineIndex)
                    jsonObject.put("sdp", p0.sdp)
                    if (!isHost) {
                        val iceCandidateSignalMessage = IceCandidateSignalMessage(
                            targetPhoneNumber!!,
                            callerPhoneNumber!!,
                            jsonObject.toString()
                        )
                        signalClient.send(iceCandidateSignalMessage)
                    } else {
                        val iceCandidateSignalMessage = IceCandidateSignalMessage(
                            callerPhoneNumber!!,
                            targetPhoneNumber!!,
                            jsonObject.toString()
                        )
                        signalClient.send(iceCandidateSignalMessage)
                    }

                }
            }
        }
        rtcClient = RTCClient(observer, context)
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


    private fun setStatus(status: CallStatus) {
        this.status = status
        statusUpdateListeners.forEach { it.invoke(status) }
    }


    override fun onCallResponseMessageReceived(callResponseSignalMessage: CallResponseSignalMessage) {
        if(callResponseSignalMessage.isAllowed()){
            setStatus(CallStatus.CONNECTING)

            val sdp = SessionDescription(SessionDescription.Type.OFFER, callResponseSignalMessage.SDP)
            rtcClient.onRemoteSessionReceived(sdp)

        } else{
            setStatus(CallStatus.ENDED)
        }
    }


    override fun onIceCandidateMessageReceived(iceCandidateSignalMessage: IceCandidateSignalMessage) {
        val json = JSONObject(iceCandidateSignalMessage.CANDIDATE)

        val iceCandidate = IceCandidate(json.getString("sdpMid"), json.getInt("sdpMLineIndex"), json.getString("sdp"))
        rtcClient.addIceCandidate(iceCandidate)
    }


    override fun onHangupMessageReceived(hangupSignalMessage: HangupSignalMessage) {
        TODO("Not yet implemented")
    }

}

enum class CallStatus {
    CREATED,
    IN_CALL,
    RECEIVING,
    REQUESTING,
    CONNECTING,
    DENIED,
    ENDED;
}