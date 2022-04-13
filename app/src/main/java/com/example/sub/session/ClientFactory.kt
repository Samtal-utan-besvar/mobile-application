package com.example.sub.session

import android.util.Log
import com.example.sub.signal.SignalClient

object ClientFactory {

    private var _signalClient: SignalClient? = null
    private var _callHandler: CallHandler? = null

    fun getSignalClient(): SignalClient {
        if (_signalClient == null) {
            _signalClient = SignalClient()
            Log.d("", "Create new client!")
        }
        return _signalClient!!
    }

    fun getCallHandler(): CallHandler {
        if (_callHandler == null) {
            _callHandler = CallHandler(getSignalClient())
            Log.d("", "Create new callHandler!")
        }
        return _callHandler!!
    }
}