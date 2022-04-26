package com.example.sub.session

/**
 * This class can be implemented and overrode by a class that wants
 * to listen to events of a [CallSession].
 */
interface SessionListener {

    /**
     * Gets called when the [CallStatus] of a [CallSession] is changed.
     */
    fun onSessionStatusChanged(callStatus: CallStatus) {}

    /**
     * Gets called when the call is denied by one of the peers.
     */
    fun onSessionsDenied() {}

    /**
     * Gets called when the call has been answered and the session tries to establish the connection.
     */
    fun onSessionConnecting() {}

    /**
     * Gets called when the connection is complete.
     */
    fun onSessionConnected() {}

    /**
     * Gets called when the session has ended for any reason. Either when the call is denied or
     * ended by one of the peers, or when a failure occurs.
     */
    fun onSessionEnded() {}

    /**
     * Gets called when an error occurs with the establishment of the call connection.
     */
    fun onSessionFailed() {}

    /**
     * Gets called when a string message is received over the webRTC connection.
     */
    fun onStringMessage(message: String) {}

    /**
     * Gets called when a bytearray message is received over the webRTC connection.
     */
    fun onBytesMessage(bytes: ByteArray) {}

}