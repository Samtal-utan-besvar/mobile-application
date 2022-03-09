package com.example.sub

import android.R.attr.data
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [callingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class callingFragment : Fragment() {
    private var navController: NavController? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_calling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController(view.findViewById(R.id.closeCall))
        view.findViewById<View>(R.id.closeCall).setOnClickListener {

            // TODO: Action when close call, disconnect call from server??

            navController!!.navigate(
                R.id.action_callingFragment_to_userProfile
            )
        }

        // onClick for Speaker toggleButton
        val toggleButtonSilentMode: ToggleButton = view.findViewById(R.id.toggleButtonSilentMode)
        toggleButtonSilentMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Action when speaker is on
                Toast.makeText(activity, "speaker on", Toast.LENGTH_LONG).show()    // remove
            } else {
                // TODO: Action when speaker is off
                Toast.makeText(activity, "speaker of", Toast.LENGTH_LONG).show()    // remove
            }
        }

        // onClick for Mute toggleButton
        val toggleButtonMute: ToggleButton = view.findViewById(R.id.toggleButtonMute)
        toggleButtonMute.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Action when un-muted
                Toast.makeText(activity, "un-mute", Toast.LENGTH_LONG).show()       // remove
            } else {
                // TODO: Action when muted
                Toast.makeText(activity, "mute", Toast.LENGTH_LONG).show()          // remove
            }
        }

        // Timer
        // TODO: place in a suitable place, timer should start when phone call starts, not when this fragment is created
        val simpleChronometer =
            view.findViewById(R.id.simpleChronometer) as Chronometer // initiate a chronometer
        simpleChronometer.start() // start a chronometer
    }

        companion object {
        fun newInstance(): callingFragment {
            return callingFragment()
        }
    }
}

