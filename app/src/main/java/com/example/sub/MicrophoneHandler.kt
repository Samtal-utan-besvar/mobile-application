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

class MicrophoneHandler() {
    private var recordingThread: Deferred<ByteArrayOutputStream>? = null
    val sampleRate = 16000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    var recording = AtomicBoolean(true)

    fun StartAudioRecording(){
        recording.set(true)

        recordingThread = GlobalScope.async {
            WriteAudioToDataFile(recording)
        }
        Log.e("blabla", "nöanöanöa")

    }

    fun StopAudioRecording(): ByteArray {

        recording.set(false)
        var soundBytes = ByteArrayOutputStream()
        GlobalScope.async {soundBytes = recordingThread!!.await()}

        return soundBytes.toByteArray()


    }


    @SuppressLint("MissingPermission")
    fun WriteAudioToDataFile(recording: AtomicBoolean): ByteArrayOutputStream {

        Log.e("Thread", "started")
        var bigBuffer = ByteArrayOutputStream()

        var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        var buffer = ByteArray(minBufferSize)

        var microphone = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufferSize * 10)

        if (microphone!!.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("microphone did not start recording", "error initializing AudioRecord");
            return ByteArrayOutputStream()
        }

        microphone.startRecording()

        while (recording.get()) {

            val read = microphone!!.read(buffer, 0, buffer.size)
            bigBuffer.write(buffer, 0, read)
            //Log.e("Smolbuf", buffer.toString())
        }
        bigBuffer.flush()
        Log.e("Thread", "stopped")
        microphone!!.stop()
        microphone!!.release()
        Log.e("Bigbuffer in thread", bigBuffer.size().toString())
        return bigBuffer
    }

}


