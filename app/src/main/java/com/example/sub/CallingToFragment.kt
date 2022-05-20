package com.example.sub
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.fragment.app.Fragment

import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sub.session.CallHandler
import com.example.sub.session.CallSession
import com.example.sub.session.CallStatus
import com.example.sub.session.SessionListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class CallingToFragment : Fragment() {
    var navController: NavController? = null
    private lateinit var profileFirstName: String
    private lateinit var profileLastName : String
    private lateinit var profilePhoneNumber : String
    private lateinit var userName : TextView

    var isClosed = false
    var session : CallSession? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_calling_to, container, false)
    }

    /** Function that runs the necessary functions to show correct information on a contact users
     * profile
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userName = view.findViewById(R.id.calling_to_name)
        profileFirstName = arguments?.getString("first_name")!!
        profileLastName = arguments?.getString("last_name")!!
        profilePhoneNumber = arguments?.getString("phone_nr")!!
        userName.text = "Ringer " + profileFirstName + " " + profileLastName

        session = CallHandler.getInstance().activeSession

        val bundle = Bundle()
        bundle.putString("first_name", profileFirstName as String?)
        bundle.putString("last_name", profileLastName as String?)
        bundle.putString("phone_nr", profilePhoneNumber as String?)
        navController = Navigation.findNavController(view.findViewById(R.id.stopCalling))

        view.findViewById<View>(R.id.stopCalling).setOnClickListener {

            //navController?.navigate(R.id.action_callingToFragment_to_userProfileFragment, bundle)
            closeView()
        }

        session?.addListener(SessionHandler())
    }


    override fun onDestroy() {
        if (session?.getStatus() != CallStatus.IN_CALL) {
            session?.hangUp()
        }
        super.onDestroy()
    }


    /**
     * Navigates back to the previous view.
     */
    fun closeView() {
        if (!isClosed) {
            isClosed = true
            GlobalScope.launch {
                try {
                    navController?.popBackStack()
                } catch (e: Exception) {
                }
            }
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
                    //getActivity()?.getSupportFragmentManager()?.popBackStack();
                    navController?.navigate(R.id.action_callingToFragment_to_callingFragment, bundle)
                } catch (e: Exception) {}
            }
        }

        override fun onSessionEnded() {
            closeView()
        }

    }
}











