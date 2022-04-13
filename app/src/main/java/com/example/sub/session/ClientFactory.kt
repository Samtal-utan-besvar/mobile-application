package com.example.sub.session

import android.util.Log
import com.example.sub.signal.SignalClient

object ClientFactory {

    private var _signalClient: SignalClient? = null

    fun getSignalClient(): SignalClient {
        if (_signalClient == null) {
            _signalClient = SignalClient()
            Log.d("", "Create new client!")
        }
        return _signalClient!!
    }


}