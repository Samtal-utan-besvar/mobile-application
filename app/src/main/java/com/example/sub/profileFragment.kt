package com.example.sub

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController


/**
 * A simple [Fragment] subclass.
 * Use the [profileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class profileFragment : Fragment() {

    //Define AudioRecord Object and other parameters

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

                var callHandler = MicrophoneHandler()

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        callHandler.StartAudioRecording()
                        transcribeButton.text = "recording"


                    }
                    MotionEvent.ACTION_UP -> {

                        transcribeButton.text = "press to record"
                        callHandler.StopAudioRecording()


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
