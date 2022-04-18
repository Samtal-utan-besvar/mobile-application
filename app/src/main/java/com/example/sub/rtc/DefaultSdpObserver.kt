package com.example.sub.rtc

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class DefaultSdpObserver : SdpObserver {
    override fun onCreateSuccess(p0: SessionDescription?) {
        Log.d("SdpObserver", "create success")
    }

    override fun onSetSuccess() {
        Log.d("SdpObserver", "set success")
    }

    override fun onCreateFailure(p0: String?) {
        Log.d("SdpObserver", "create failure")
    }

    override fun onSetFailure(p0: String?) {
        Log.d("SdpObserver", "set failure")
    }
}