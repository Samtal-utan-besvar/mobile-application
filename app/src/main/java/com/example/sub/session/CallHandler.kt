package com.example.sub.session

import android.content.Context
import android.util.Log
import com.example.sub.signal.*

// This class handles incoming and outgoing calls
class CallHandler(signalClient: SignalClient) : SignalListener{

    var callReceivedListener = ArrayList<(CallSession) -> Unit>()
    private var activeSession: CallSession? = null
    private var signalClient: SignalClient = signalClient
    private var context: Context? = null

    init{
        signalClient.signalListeners.add(this)
    }

    fun setContext(context: Context) {
        this.context = context
    }

    fun call(phoneNumber: String): CallSession {
        val call: CallSession = CallSession(signalClient, context!!)
        call.startCall("", phoneNumber)
        return call
    }

    override fun onCallMessageReceived(callSignalMessage: CallSignalMessage) {
        // Call session already active
        if(activeSession != null){
            signalClient.sendCallResponseMessage(callSignalMessage.toResponse(false));
        } else {
            activeSession = CallSession(signalClient, context!!)
            activeSession!!.answerCall(callSignalMessage)

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