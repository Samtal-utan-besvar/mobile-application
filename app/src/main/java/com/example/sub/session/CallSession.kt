package com.example.sub.session

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.sub.rtc.*
import com.example.sub.signal.*
import kotlinx.serialization.json.buildJsonObject
import org.json.JSONObject
import org.webrtc.*

// Class representing an existing or pending call session
class CallSession(private var signalClient: SignalClient, private var context: Context): SignalListener {

    private var rtcClient: RTCClient


    private var isHost = false
    private var callerPhoneNumber: String? = null
    private var targetPhoneNumber: String? = null


    private var status: CallStatus = CallStatus.CREATED
    var statusUpdateListeners = ArrayList<(CallStatus) -> Unit>()


    init {
        val observer = object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                if (p0 != null) {
                    val jsonCandidate = buildJsonObject {
                        "sdpMid" to p0.sdpMid
                        "sdpMLineIndex" to p0.sdpMLineIndex
                        "sdp" to p0.sdp
                    }.toString()
                    val iceCandidateSignalMessage = IceCandidateSignalMessage(callerPhoneNumber!!, targetPhoneNumber!!, jsonCandidate)
                    signalClient.send(iceCandidateSignalMessage)
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
                        CallResponseSignalMessage(CallResponse.ALLOW, callerPhoneNumber!!, targetPhoneNumber!!, p0)
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