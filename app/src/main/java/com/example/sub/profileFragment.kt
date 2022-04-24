package com.example.sub

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [profileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class profileFragment : Fragment() {
    var navController: NavController? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("MissingPermission", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController(view.findViewById(R.id.AnnaKnappen))
        view.findViewById<View>(R.id.AnnaKnappen).setOnClickListener {
            navController!!.navigate(
                R.id.action_profileFragment_to_userProfile
            )
        }
        val transcribeButton = view.findViewById<Button>(R.id.buttonTranscribe)

        transcribeButton.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val sampleRate = 48000
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT

                val minBufferSize =
                    AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                val microphone = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufferSize * 10
                )


                val buffer = ShortArray(1024)
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {

                        transcribeButton.text = "recording"
                        microphone.startRecording()
                        Log.d("hehe", "hehehehe")

                    }
                    MotionEvent.ACTION_UP -> {

                        transcribeButton.text = "press to record"
                        Log.d("buffer: ", buffer.toString())
                        //this format for read is probably wrong
                        //not sure how to handle the data or if you are even supposed
                        // to do it like this

                        microphone.read(buffer, 0, minBufferSize)
                        microphone.stop()
                        microphone.release()
                        //send buffer to server
                    }

                }

                return v?.onTouchEvent(event) ?: true
            }
        })

    }

    companion object {
        fun newInstance(): callingFragment {
            return callingFragment()
        }
    }
}
