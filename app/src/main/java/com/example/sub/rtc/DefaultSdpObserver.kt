package com.example.sub.rtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class DefaultSdpObserver : SdpObserver {
    override fun onCreateSuccess(p0: SessionDescription?) {}

    override fun onSetSuccess() {}

    override fun onCreateFailure(p0: String?) {}

    override fun onSetFailure(p0: String?) {}
}