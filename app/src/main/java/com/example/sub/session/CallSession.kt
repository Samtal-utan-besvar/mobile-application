package com.example.sub.session

import com.example.sub.signal.*

// Class representing an existing or pending call session
class CallSession(signalClient: SignalClient, callSignalMessage: CallSignalMessage, isHost: Boolean): SignalListener {
    private var signalClient = signalClient
    private var callSignalMessage = callSignalMessage
    private var isHost = isHost

    override fun onCallResponseMessageReceived(callResponseSignalMessage: CallResponseSignalMessage) {
        if(!isHost){
            if(callResponseSignalMessage.isAllowed()){

            } else{

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