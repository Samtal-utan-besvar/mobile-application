package com.example.sub.signal

interface SignalListener {
    fun onCallMessageReceived (callSignalMessage: CallSignalMessage){}

    fun onCallResponseMessageReceived (callResponseSignalMessage: CallResponseSignalMessage){}

    fun onIceCandidateMessageReceived (iceCandidateSignalMessage: IceCandidateSignalMessage){}

    fun onHangupMessageReceived (hangupSignalMessage: HangupSignalMessage){}
}