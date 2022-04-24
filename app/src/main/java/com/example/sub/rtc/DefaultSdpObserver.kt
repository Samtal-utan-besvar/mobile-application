package com.example.sub.rtc

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

/**
 * The default observer for tasks related to [SessionDescription]. This observer simply logs
 * the different function calls. The class can be implemented and overrode to add other
 * more functionality.
 */
open class DefaultSdpObserver : SdpObserver {

    /**
     * Gets called when a [SessionDescription] has been successfully created.
     */
    override fun onCreateSuccess(p0: SessionDescription?) {
        Log.d("SdpObserver", "create success")
    }


    /**
     * Gets called when a [SessionDescription] has been set as the local or remote description
     */
    override fun onSetSuccess() {
        Log.d("SdpObserver", "set success")
    }


    /**
     * Gets called when a [SessionDescription] has failed to be created.
     */
    override fun onCreateFailure(p0: String?) {
        Log.d("SdpObserver", "create failure")
    }


    /**
     * Gets called when a [SessionDescription] could not be set as the local or remote description.
     */
    override fun onSetFailure(p0: String?) {
        Log.d("SdpObserver", "set failure")
    }
}