package com.example.sub.session

/**
 * This interface can be implemented by a class that wants to get notified by the [CallHandler]
 * when someone is calling.
 */
interface CallReceivedListener {

    /**
     * Gets called when someone is calling. The [callSession] represents the call and has the caller
     * stored and functions to accept or deny the call.
     */
    fun onCallReceived(callSession: CallSession)

}