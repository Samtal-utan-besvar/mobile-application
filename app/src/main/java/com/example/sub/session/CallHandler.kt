package com.example.sub.session

import android.content.Context
import android.util.Log
import com.example.sub.signal.*

// This class handles incoming and outgoing calls
class CallHandler(private var signalClient: SignalClient) : SignalListener{

    var callReceivedListener = ArrayList<(CallSession) -> Unit>()
    private var activeSession: CallSession? = null
    private var context: Context? = null

    init{
        signalClient.signalListeners.add(this)
    }

    fun setContext(context: Context) {
        this.context = context
    }

    fun call(callerPhoneNumber: String, targetPhoneNumber: String): CallSession? {
        if (activeSession == null) {
            val call: CallSession = CallSession(signalClient, context!!)
            call.requestCall(callerPhoneNumber, targetPhoneNumber)
            activeSession = call
            return call
        } else {
            Log.e("CallHandler", "There is already an active session")
            return null
        }
    }

    override fun onCallMessageReceived(callSignalMessage: CallSignalMessage) {
        // Call session already active
        if(activeSession != null){
            signalClient.send(callSignalMessage.toResponse(CallResponse.DENY));
        } else {
            activeSession = CallSession(signalClient, context!!)
            activeSession!!.receiveCall(callSignalMessage)

            callReceivedListener.forEach {
                it.invoke(activeSession!!)
            }
        }
    }

    override fun onCallResponseMessageReceived(callResponseSignalMessage: CallResponseSignalMessage) {
        activeSession?.onCallResponseMessageReceived(callResponseSignalMessage)
    }

    override fun onIceCandidateMessageReceived(iceCandidateSignalMessage: IceCandidateSignalMessage) {
        activeSession?.onIceCandidateMessageReceived(iceCandidateSignalMessage)
    }

    override fun onHangupMessageReceived(hangupSignalMessage: HangupSignalMessage) {
        activeSession?.onHangupMessageReceived(hangupSignalMessage)
    }


}