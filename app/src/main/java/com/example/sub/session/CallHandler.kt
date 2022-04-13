package com.example.sub.session

import android.util.Log
import com.example.sub.signal.*

// This class handles incoming and outgoing calls
class CallHandler(signalClient: SignalClient) : SignalListener{

    var callReceivedListener = ArrayList<(CallSession) -> Unit>()
    private var activeSession: CallSession? = null
    private var signalClient: SignalClient = signalClient

    init{
        signalClient.signalListeners.add(this)
    }

    //fun Call(phoneNumber: String): CallSession {
        //val call: CallSession = CallSession()
        //return call
    //}

    override fun onCallMessageReceived(callSignalMessage: CallSignalMessage) {
        // Call session already active
        if(activeSession != null){
            signalClient.sendCallResponseMessage(callSignalMessage.toResponse(false));
        } else {
            activeSession = CallSession(signalClient, callSignalMessage, false)

            callReceivedListener.forEach {
                it.invoke(activeSession!!)
            }
        }
    }

    override fun onCallResponseMessageReceived(callResponseSignalMessage: CallResponseSignalMessage) {
        TODO("Not yet implemented")
    }

    override fun onIceCandidateMessageReceived(iceCandidateSignalMessage: IceCandidateSignalMessage) {
        TODO("Not yet implemented")
    }

    override fun onHangupMessageReceived(hangupSignalMessage: HangupSignalMessage) {
        TODO("Not yet implemented")
    }


}