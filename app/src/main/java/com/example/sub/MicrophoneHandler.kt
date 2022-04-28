package com.example.sub

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.sub.transcription.TranscriptionClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.min

class MicrophoneHandler() {
    private var recordingThread: Deferred<String>? = null
    val sampleRate = 16000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    @Volatile var recording = AtomicBoolean(true)

    fun StartAudioRecording(){
        recording.set(true)

        recordingThread = GlobalScope.async {
            WriteAudioToDataFile(recording)
        }


    }

    fun StopAudioRecording(): String {

        recording.set(false)
        var soundBytes : String
        runBlocking {soundBytes = recordingThread!!.await()}

        return soundBytes


    }


    @SuppressLint("MissingPermission")
    fun WriteAudioToDataFile(recording: AtomicBoolean): String {

        var bigBuffer = ByteArrayOutputStream()

        var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        var buffer = ByteArray(minBufferSize)

        var microphone = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufferSize * 10)

        if (microphone!!.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("microphone did not start recording", "error initializing AudioRecord");
            var empty = ""
            return empty
        }

        microphone.startRecording()

        while (recording.get()) {

            val read = microphone!!.read(buffer, 0, minBufferSize)
            bigBuffer.write(buffer, 0, minBufferSize)
        }
        bigBuffer.flush()
        bigBuffer.close()
        microphone!!.stop()
        microphone!!.release()
        return bigBuffer.toByteArray().toString(Charsets.ISO_8859_1)
    }

}


