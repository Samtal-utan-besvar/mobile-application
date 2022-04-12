package com.example.sub.session

import android.util.Log
import com.example.sub.SignalingClient

object ClientFactory {

    private var _signalClient: SignalingClient? = null

    fun getSignalClient(): SignalingClient {
        if (_signalClient == null) {
            _signalClient = SignalingClient()
            Log.d("", "Create new client!")
        }
        return _signalClient!!
    }


}