package com.example.sub.session

import android.content.Context
import android.util.Log
import com.example.sub.signal.*

/**
 * This class handles incoming and outgoing calls and keeps track of any active call.
 */
class CallHandler private constructor(
    private val signalClient: SignalClient,
    private val localPhoneNumber: String
    ) : SignalMessageListener, SessionListener {

    var callReceivedListeners = ArrayList<CallReceivedListener>()
    var activeSession: CallSession? = null
        private set(session) {
            field?.sessionListeners?.remove(this)
            field = session
            session?.sessionListeners?.add(this)
            Log.d("CallHandler", "Active session changed to: $activeSession")
        }


    init {
        signalClient.signalListeners.add(this)
    }


    companion object {

        @Volatile
        private var instance: CallHandler? = null


        /**
         * Initializes a singleton instance of the [CallHandler].
         */
        fun initInstance(signalClient: SignalClient, localPhoneNumber: String) {
            instance = CallHandler(signalClient, localPhoneNumber)
        }


        /**
         * Returns the singleton instance of the [CallHandler]. If [initInstance] has not been
         * called, this function will fail as the [CallHandler] instance is not initialized yet.
         */
        fun getInstance() : CallHandler {
            if (instance == null) throw UnInitializedException(
                "CallHandler.initInstance must be called before the use of getInstance")
            else return instance!!
        }


        /**
         * Exception thrown when [getInstance] is called before [initInstance].
         */
        class UnInitializedException(message: String) : Exception(message) {
            init {
                stackTrace = stackTrace.copyOfRange(1, stackTrace.size - 1)
            }
        }

    }


    /**
     * Returns true if there is an active session, else false.
     */
    fun hasActiveSession() : Boolean {
        return activeSession != null
    }


    /**
     * Starts and returns a [CallSession] with the given phone number.
     */
    fun call(targetPhoneNumber: String, context: Context): CallSession? {

        if (hasActiveSession()) {
            // Already has an active session.
            Log.e("CallHandler", "There is already an active session")
            return null
        } else {
            // No active session. OK to start new one.
            activeSession = CallSession.asCaller(signalClient!!, localPhoneNumber!!,
                targetPhoneNumber, context)
            return activeSession
        }
    }


    /**
     * Handles [CallMessage] when someone calls. Notifies listeners that someones calling.
     */
    override fun onCallMessageReceived(callMessage: CallMessage) {

        if(activeSession != null){
            // Already have an active call session.
            signalClient!!.send(callMessage.toResponse(CallResponse.DENY));
        } else {
            // Okay to create a new call session.
            val localPhoneNumber = callMessage.TARGET_PHONE_NUMBER
            val remotePhoneNumber = callMessage.CALLER_PHONE_NUMBER
            val sdp = callMessage.toSessionDescription()

            activeSession = CallSession.asReceiver(signalClient!!, localPhoneNumber,
                remotePhoneNumber, sdp)

            // Notify that there is a new session.
            callReceivedListeners.forEach {
                it.onCallReceived(activeSession!!)
            }
        }
    }


    /**
     * Handles [CallResponseMessage] when receiving an answer on a started call.
     */
    override fun onCallResponseMessageReceived(callResponseMessage: CallResponseMessage) {
        activeSession?.onCallResponseMessageReceived(callResponseMessage)
    }


    /**
     * Handles [IceCandidateMessage] when receiving remote ice candidates.
     */
    override fun onIceCandidateMessageReceived(iceCandidateMessage: IceCandidateMessage) {
        activeSession?.onIceCandidateMessageReceived(iceCandidateMessage)
    }


    /**
     * Handles [HangupMessage] when receiving a message that someone wants to end a call.
     */
    override fun onHangupMessageReceived(hangupMessage: HangupMessage) {
        activeSession?.onHangupMessageReceived(hangupMessage)
    }


    override fun onSessionEnded() {
        activeSession = null
    }


}