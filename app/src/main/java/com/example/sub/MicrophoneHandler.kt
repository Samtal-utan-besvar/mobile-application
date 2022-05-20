package com.example.sub

import android.annotation.SuppressLint
import android.media.*
import android.util.Log
import android.view.View
import com.example.sub.transcription.TranscriptionClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.min

class MicrophoneHandler() {

    private var recordingThread: Deferred<ByteArray>? = null
    val sampleRate = 16000
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private @Volatile var recording = AtomicBoolean(true)
    private @Volatile var extractData = AtomicBoolean(false)
    private val channel = Channel<ByteArray>()

    fun getRecordingStatus() : Boolean{
        return recording.get()
    }

    /*
    Starts a thread that records microphone.
     */
    fun StartAudioRecording(){
        recording.set(true)
        recordingThread = GlobalScope.async {
            WriteAudioToDataFile(recording, extractData, channel)
        }
    }

    /*
    Stops the thead started in start function. Records the sound in an bytearray.
     */
    fun StopAudioRecording(): ByteArray {

        recording.set(false)
        var soundBytes : ByteArray
        runBlocking {soundBytes = recordingThread!!.await()}

        return soundBytes
    }

    fun extractData(): ByteArray{
        extractData.set(true)
        var soundData : ByteArray
        runBlocking {soundData = channel.receive()}
        return soundData
    }

    /*
    A thread is started here that puts all of the input an bytearrayoutputstream. When stop is called,
    the thread exists and return all of the recorded sound in a bytearray.
     */
    @SuppressLint("MissingPermission")
    suspend fun WriteAudioToDataFile(recording: AtomicBoolean, extract: AtomicBoolean, chan: Channel<ByteArray>): ByteArray {

        var bigBuffer = ByteArrayOutputStream()

        var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        var buffer = ByteArray(minBufferSize)

        var microphone = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufferSize * 10)

        if (microphone!!.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("microphone did not start recording", "error initializing AudioRecord");
            var empty = ""
            return ByteArray(0)
        }

        microphone.startRecording()

        while (recording.get()) {

            val read = microphone!!.read(buffer, 0, minBufferSize)
            bigBuffer.write(buffer, 0, read)

            //If true, 5 seconds has passed and data needs to be extracted and the buffer reset
            if (extract.get()){
                bigBuffer.flush()
                bigBuffer.close()
                chan.send(bigBuffer.toByteArray())
                bigBuffer = ByteArrayOutputStream()
                extract.set(false)
            }
        }
        bigBuffer.flush()
        bigBuffer.close()
        microphone!!.stop()
        microphone!!.release()
        return bigBuffer.toByteArray()
    }

    fun close(){
        recording.set(false)
        recordingThread?.cancel(null)
    }

}


