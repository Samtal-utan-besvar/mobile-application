package com.example.sub

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.sub.transcription.TranscriptionClient
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

class MicrophoneHandler() {
    lateinit var microphone: AudioRecord
    var minBufferSize: Int
    var recording = false
    lateinit var bigBuffer: ByteArrayOutputStream
    private var recordingThread: Thread? = null
    init {
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        @SuppressLint("MissingPermission")
        microphone = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufferSize * 10)

        bigBuffer = ByteArrayOutputStream()
    }
    fun StartAudioRecording(){
        bigBuffer = ByteArrayOutputStream()
        recording = true
        if (microphone!!.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("microphone did not start recording", "error initializing AudioRecord");
            return
        }

        microphone.startRecording()

        recordingThread = thread(true){
            WriteAudioToDataFile()
        }
    }
    fun StopAudioRecording(): ByteArrayOutputStream {

        if (microphone!= null) {
            recording = false
            recordingThread = null
            microphone!!.stop()
            microphone!!.release()
// triggers recordingThread to exit while loop
        }
        return bigBuffer
    }
    fun WriteAudioToDataFile(){
        var buffer = ByteArray(1024)
        while (recording) {
            microphone!!.read(buffer, 0, minBufferSize)
            bigBuffer.write(buffer, 0, minBufferSize)
            Log.d("smolbuffer: ", buffer.toString())
        }
        //write to buffer
    }

}


