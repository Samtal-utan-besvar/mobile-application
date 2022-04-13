package com.example.sub.session

// This class handles incoming and outgoing calls
class CallHandler {

    private var onReceivingCallFunction: ((CallSession) -> Unit)? = null


    fun setOnReceivingCall(func: (CallSession) -> Unit) {
        onReceivingCallFunction = func
    }


    private fun onReceivingCall() {

        val call: CallSession = CallSession()

        onReceivingCallFunction?.invoke(call)
    }


    fun Call(phoneNumber: String): CallSession {

        val call: CallSession = CallSession()

        return call
    }


}