package com.example.sub.signal

/**
 * This interface can be implemented by a class that wants to get notified when messages are
 * received from the signal server.
 */
interface SignalMessageListener {

    /**
     * Gets called when the [SignalClient] receives a [CallMessage].
     */
    fun onCallMessageReceived (callMessage: CallMessage){}


    /**
     * Gets called when the [SignalClient] receives a [CallResponseMessage].
     */
    fun onCallResponseMessageReceived (callResponseMessage: CallResponseMessage){}


    /**
     * Gets called when the [SignalClient] receives a [IceCandidateMessage].
     */
    fun onIceCandidateMessageReceived (iceCandidateMessage: IceCandidateMessage){}


    /**
     * Gets called when the [SignalClient] receives a [HangupMessage].
     */
    fun onHangupMessageReceived (hangupMessage: HangupMessage){}
}