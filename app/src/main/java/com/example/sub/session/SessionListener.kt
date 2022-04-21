package com.example.sub.session

open class SessionListener {

    fun onSessionStatusChanged(callStatus: CallStatus) {}

    fun onSessionsDenied() {}

    fun onSessionConnecting() {}

    fun onSessionConnected() {}

    fun onSessionEnded() {}

    fun onSessionFailed() {}

}