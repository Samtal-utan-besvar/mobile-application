package com.example.sub.transcription

interface TranscriptionListener {
    fun onCallMessageReceived (callSignalMessage: CallSignalMessage){}
}