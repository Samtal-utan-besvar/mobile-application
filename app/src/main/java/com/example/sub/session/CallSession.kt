package com.example.sub.session

import android.content.Context
import android.util.Log
import com.example.sub.rtc.*
import com.example.sub.signal.*
import org.webrtc.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * An instance of this class represents a call session. It uses a [SignalClient] to communicate
 * and exchange messages necessary to establish a connection with webRTC.
 */
class CallSession private constructor(val signalClient: SignalClient,
                  val localPhoneNumber: String, val remotePhoneNumber: String
) : SignalMessageListener {

    private var rtcClient: RTCClient? = null
    private var remoteSDP: SessionDescription? = null

    private var isCaller = false
    private var status: CallStatus = CallStatus.CREATED

    // Queue for ice candidates to send.
    private val waitingIceCandidates : Queue<IceCandidateMessage> = LinkedList()
    private var hasReceivedIce = false

    // List of listeners for getting notified of changes to the session.
    private var sessionListeners = ArrayList<SessionListener>()


    /**
     * Adds a [SessionListener] to the list of listeners
     */
    fun addListener(sessionListener: SessionListener) {
        sessionListeners.add(sessionListener)
    }


    // Observer for the webRTC connection.
    inner class DefaultPeerConnectionObserver : PeerConnectionObserver {

        /**
         * Gets called when the state of the peer connection is changed. Sets the status of
         * the session depending on the [PeerConnection.IceConnectionState].
         */
        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            Log.d("RTCClient-ice", p0.toString())
            if (p0 == PeerConnection.IceConnectionState.CONNECTED) setStatus(CallStatus.IN_CALL)
        }


        /**
         * Gets called when the [PeerConnection] finds a new [IceCandidate]. This candidate should
         * be sent to the other peer over the signal server.
         */
        override fun onIceCandidate(p0: IceCandidate?) {
            if (p0 != null) {
                val iceCandidateSignalMessage = IceCandidateMessage.fromIceCandidate(p0, localPhoneNumber, remotePhoneNumber)
                queueIceCandidate(iceCandidateSignalMessage)
            }
        }


        /**
         * Gets called when a [ByteArray] is received over the [PeerConnection].
         */
        override fun onBytesMessage(p0: ByteArray) {
            super.onBytesMessage(p0)
            sessionListeners.forEach { it.onBytesMessage(p0) }
        }


        /**
         * Gets called when a [String] is received over the [PeerConnection].
         */
        override fun onStringMessage(p0: String) {
            super.onStringMessage(p0)
            sessionListeners.forEach { it.onStringMessage(p0) }
        }
    }


    /**
     * Tells the session to initiate as the caller.
     */
    private fun initAsCaller(context: Context) {
        isCaller = true
        setStatus(CallStatus.REQUESTING)

        rtcClient = RTCClient(DefaultPeerConnectionObserver(), context)
        rtcClient!!.call(object : DefaultSdpObserver() {

            // Gets called when the sdp has been created.
            override fun onCreateSuccess(p0: SessionDescription?) {
                super.onCreateSuccess(p0)
                if (p0 != null) {
                    val callMessage = CallMessage(localPhoneNumber, remotePhoneNumber, p0)
                    signalClient.send(callMessage)
                }
            }
        })
    }


    /**
     * Tells the session to initiate as the receiver of the call
     */
    private fun initAsReceiver(sdp: SessionDescription) {
        isCaller = false
        setStatus(CallStatus.RECEIVING)
        remoteSDP = sdp
    }


    companion object {

        /**
         * Constructs and returns a [CallSession] initiated as the caller.
         * @param localPhoneNumber the local users phone number.
         * @param remotePhoneNumber the phone number that should be called.
         */
        fun asCaller(signalClient: SignalClient, localPhoneNumber: String,
                     remotePhoneNumber: String, context: Context
        ) : CallSession {
            val call = CallSession(signalClient, localPhoneNumber, remotePhoneNumber)
            call.initAsCaller(context)
            return call
        }


        /**
         * Constructs and returns a [CallSession] initiated as the receiver.
         * @param localPhoneNumber the local users phone number.
         * @param remotePhoneNumber the phone number that should be called.
         */
        fun asReceiver(signalClient: SignalClient, localPhoneNumber: String,
                       remotePhoneNumber: String, sdp: SessionDescription
        ) : CallSession {
            val call = CallSession(signalClient, localPhoneNumber, remotePhoneNumber)
            call.initAsReceiver(sdp)
            return call
        }
    }


    /**
     * Sends the given [bytes] to the other peer through webRTC.
     */
    fun sendBytes(bytes: ByteArray) = rtcClient?.sendBytes(bytes)


    /**
     * Sends the given [message] to the other peer through webRTC.
     */
    fun sendString(message: String) = rtcClient?.sendString(message)


    /**
     * Sets the [CallStatus] of the session.
     */
    private fun setStatus(status: CallStatus) {
        if (status != this.status) {
            this.status = status

            onStatusChanged(status)
        }
    }


    /**
     * Gets called when the [CallStatus] of the session is changed
     */
    private fun onStatusChanged(callStatus: CallStatus) {
        Log.d("CallSession", "call status changed to: $callStatus")
        // Notify listeners.
        when (status) {
            CallStatus.CONNECTING -> sessionListeners.forEach { it.onSessionConnecting() }
            CallStatus.IN_CALL -> sessionListeners.forEach { it.onSessionConnected() }
            CallStatus.DENIED -> {
                sessionListeners.forEach { it.onSessionsDenied() }
                endSession()
            }
            CallStatus.FAILED -> {
                sessionListeners.forEach { it.onSessionFailed() }
                endSession()
            }
            CallStatus.ENDED -> endSession()
            else -> {}
        }

        sessionListeners.forEach{ it.onSessionStatusChanged(callStatus) }

        sendWaitingIceCandidates()
    }


    /**
     * Accepts the call and continues to set up a connection. Requires a [Context] for the call.
     */
    fun accept(context: Context) {

        // Create and set up the webRTC client.
        rtcClient = RTCClient(DefaultPeerConnectionObserver(), context)
        rtcClient!!.onRemoteSessionReceived(remoteSDP!!)
        rtcClient!!.answer(object : DefaultSdpObserver() {

            // Gets called when the sdp has been created.
            override fun onCreateSuccess(p0: SessionDescription?) {
                if (p0 != null) {
                    val callMessage = CallResponseMessage(CallResponse.ACCEPT,
                        remotePhoneNumber, localPhoneNumber, p0)
                    signalClient.send(callMessage)
                }
            }
        })
        setStatus(CallStatus.CONNECTING)
    }


    /**
     * Denies the call.
     */
    fun deny() {
        val callMessage =
            CallResponseMessage(CallResponse.DENY, remotePhoneNumber, localPhoneNumber)
        signalClient.send(callMessage)
        setStatus(CallStatus.DENIED)
    }


    /**
     * Hangs up the call
     */
    fun hangUp() {
        signalClient.send(HangupMessage(localPhoneNumber, remotePhoneNumber))
        setStatus(CallStatus.ENDED)
    }


    /**
     * Ends the session. Closes the webRTC client and notifies listeners that the session is over.
     */
    private fun endSession() {
        rtcClient?.endCall()
        sessionListeners.forEach {
            it.onSessionEnded()
        }
    }


    /**
     * Handles incoming [CallResponseMessage]. Continues to set up communication if opponent
     * accepted the call and otherwise stops the session.
     */
    override fun onCallResponseMessageReceived(callResponseMessage: CallResponseMessage) {
        if(callResponseMessage.isAllowed()) {
            setStatus(CallStatus.CONNECTING)

            val sdp = callResponseMessage.toSessionDescription()
            rtcClient!!.onRemoteSessionReceived(sdp)

        } else{
            setStatus(CallStatus.DENIED)
        }
    }


    /**
     * Handles incoming [IceCandidateMessage]. Adds the remote [IceCandidate] to the webRTC client.
     */
    override fun onIceCandidateMessageReceived(iceCandidateMessage: IceCandidateMessage) {
        val iceCandidate = iceCandidateMessage.toIceCandidate()
        rtcClient?.addIceCandidate(iceCandidate)


        // Send the local ice candidates if any waiting.
        if (!hasReceivedIce) {
            hasReceivedIce = true
            sendWaitingIceCandidates()
        }
    }


    /**
     * Handles incoming [HangupMessage].
     */
    override fun onHangupMessageReceived(hangupMessage: HangupMessage) {
        setStatus(CallStatus.ENDED)
    }


    /**
     * Queues an [IceCandidate] for sending and tries to send all waiting candidates.
     */
    private fun queueIceCandidate(iceCandidateMessage: IceCandidateMessage) {
        waitingIceCandidates.add(iceCandidateMessage)
        sendWaitingIceCandidates()
    }


    /**
     * Sends all ice candidates that are waiting if allowed.
     */
    private fun sendWaitingIceCandidates() {
        if (canSendIce()) {
            while (!waitingIceCandidates.isEmpty()) {
                signalClient.send(waitingIceCandidates.remove())
            }
        }
    }


    /**
     * Returns true if appropriate to send ice candidates, else false.
     */
    private fun canSendIce() : Boolean {
        return (status == CallStatus.CONNECTING || status == CallStatus.IN_CALL) && (isCaller || hasReceivedIce)
    }

}


/**
 * Different statuses for the state of a call.
 */
enum class CallStatus {

    /**
     * Instance created but not fully initialized.
     */
    CREATED,

    /**
     * Initialized as receiver. No answer yet.
     */
    RECEIVING,

    /**
     * Initialized as caller. No answer yet.
     */
    REQUESTING,

    /**
     * Call has been accepted. Setting up connection.
     */
    CONNECTING,

    /**
     * Connection is established and the call is on-going.
     */
    IN_CALL,

    /**
     * The call has been denied by one of the peers.
     */
    DENIED,

    /**
     * The call has ended.
     */
    ENDED,

    /**
     * The connection for the call has failed.
     */
    FAILED
}