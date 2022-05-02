package com.example.sub
import android.os.Bundle

import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.fragment.app.Fragment

import androidx.navigation.NavController
import androidx.navigation.Navigation


class CallingToFragment : Fragment() {
    var navController: NavController? = null
    private lateinit var profileFirstName: String
    private lateinit var profileLastName : String
    private lateinit var profilePhoneNumber : String
    private lateinit var userName : TextView


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
        userName = view.findViewById(R.id.caller_name)
        profileFirstName = arguments?.getString("first_name")!!
        profileLastName = arguments?.getString("last_name")!!
        profilePhoneNumber = arguments?.getString("phone_nr")!!
        userName.text = profileFirstName


        val bundle = Bundle()
        bundle.putString("first_name", profileFirstName as String?)
        bundle.putString("last_name", profileLastName as String?)
        bundle.putString("phone_nr", profilePhoneNumber as String?)
        navController = Navigation.findNavController(view.findViewById(R.id.stopCalling))

        view.findViewById<View>(R.id.stopCalling).setOnClickListener {

            navController?.navigate(R.id.action_callingToFragment_to_userProfileFragment, bundle)

        }



    }
}











