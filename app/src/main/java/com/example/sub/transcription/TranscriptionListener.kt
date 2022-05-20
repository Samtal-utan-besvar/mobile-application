package com.example.sub.transcription


/**
 * Implement this interface to listen to when a transcription answer is received.
 * Add listener to the list in the [TranscriptionClient].
 */
interface TranscriptionListener {

    /**
     * Gets called when a transcription answer is received
     */
    fun onTranscriptionComplete(id: String, text: String)

}