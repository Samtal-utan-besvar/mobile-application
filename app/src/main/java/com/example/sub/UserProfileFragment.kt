package com.example.sub

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.google.gson.Gson

/** A UserProfileFragment used to display a contact from the contactlist **/
class UserProfileFragment : Fragment() {
    var navController: NavController? = null
    private lateinit var profileFirstName : TextView
    private lateinit var profileLastName : TextView
    private lateinit var profilePhoneNumber : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    /** Function that runs the necessary functions to show correct information on a contact users
     * profile
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController(view.findViewById(R.id.callButton))
        profileFirstName = view.findViewById(R.id.profileFName)
        profileLastName = view.findViewById(R.id.profileLName)
        profilePhoneNumber = view.findViewById(R.id.profilePN)
        profileFirstName.text = arguments?.getString("first_name")
        profileLastName.text = arguments?.getString("last_name")
        profilePhoneNumber.text = arguments?.getString("phone_nr")
        val bundle = Bundle()
        bundle.putString("first_name", profileFirstName.text as String?)
        bundle.putString("last_name", profileLastName.text as String?)
        bundle.putString("phone_nr", profilePhoneNumber.text as String?)

        view.findViewById<View>(R.id.callButton).setOnClickListener {

            navController?.navigate(R.id.action_userProfileFragment_to_callingFragment, bundle)

        }
        view.findViewById<View>(R.id.profile).setOnClickListener {
            navController!!.navigate(
                R.id.action_userProfileFragment_to_ProfileFragment
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    navController!!.navigate(
                        R.id.action_userProfileFragment_to_ProfileFragment
                    )
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    companion object {
        fun newInstance(): CallingFragment {
            return CallingFragment()
        }
    }
}