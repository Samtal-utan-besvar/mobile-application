package com.example.sub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sub.session.CallHandler
import com.example.sub.session.CallSession
import com.example.sub.session.CallStatus
import com.example.sub.session.SessionListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReceivingCall.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReceivingCall : Fragment() {

    var navController: NavController? = null
    private lateinit var profileFirstName: String
    private lateinit var profileLastName : String
    private lateinit var profilePhoneNumber : String

    var session : CallSession? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receiving_call, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        profileFirstName = arguments?.getString("first_name")!!
        profileLastName = arguments?.getString("last_name")!!
        profilePhoneNumber = arguments?.getString("phone_nr")!!

        session = CallHandler.getInstance().activeSession

        navController = Navigation.findNavController(view.findViewById(R.id.callButtonReceivingCall))
        view.findViewById<View>(R.id.callButtonReceivingCall).setOnClickListener {

            session?.accept(requireContext())
            it.isEnabled = false
            it.isClickable = false

        }
        navController = Navigation.findNavController(view.findViewById(R.id.refuseCallReceivingCall))
        view.findViewById<View>(R.id.refuseCallReceivingCall).setOnClickListener {
            val bundle = Bundle()
            bundle.putString("first_name", profileFirstName)
            bundle.putString("last_name", profileLastName)
            bundle.putString("phone_nr", profilePhoneNumber)
            closeView()

        }

        session?.addListener(SessionHandler())
    }

    override fun onDestroy() {
        val status = session?.getStatus()
        if (status != null && status != CallStatus.IN_CALL) {
            if (status == CallStatus.CONNECTING) {
                session?.hangUp()
            } else {
                session?.deny()
            }
        }
        super.onDestroy()
    }


    /**
     * Navigates back to the previous view.
     */
    private fun closeView() {
        GlobalScope.launch {
            try {
                navController?.popBackStack()
            } catch (e: Exception) {}
        }
    }


    inner class SessionHandler : SessionListener {

        override fun onSessionConnected() {

            val bundle = Bundle()
            bundle.putString("first_name", profileFirstName as String?)
            bundle.putString("last_name", profileLastName as String?)
            bundle.putString("phone_nr", profilePhoneNumber as String?)

            GlobalScope.launch {
                try {
                    navController?.navigate(R.id.action_receivingCall_to_callingFragment, bundle)
                } catch (e: Exception) {}
            }
        }

        override fun onSessionEnded() {
            closeView()
        }

    }


}