package com.example.sub.transcription

interface TranscriptionListener {

    fun onTranscriptionComplete(id: String, text: String)

}