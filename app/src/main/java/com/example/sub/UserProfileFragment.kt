package com.example.sub

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.example.sub.session.CallHandler
import com.example.sub.ui.login.LoginViewModel
import com.example.sub.ui.login.LoginViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking

/** A UserProfileFragment used to display a contact from the contactlist **/
class UserProfileFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    var navController: NavController? = null
    private lateinit var profileFirstName : TextView
    private lateinit var profileLastName : TextView
    private lateinit var profilePhoneNumber : TextView
    private lateinit var loginViewModel: LoginViewModel
    private val userProfileFragmentViewModel : UserProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
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
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(context))[LoginViewModel::class.java]
        runBlocking {  loginViewModel.getUser()?.userToken?.let { userProfileFragmentViewModel.setUserToken(it) }}
        view.findViewById<View>(R.id.callButton).setOnClickListener {

            callContact(profilePhoneNumber.text as String)
            navController?.navigate(R.id.action_userProfileFragment_to_callingToFragment, bundle)

        }
        view.findViewById<View>(R.id.profile).setOnClickListener {
            navController!!.navigate(
                R.id.action_userProfileFragment_to_ProfileFragment
            )
        }

        view.findViewById<View>(R.id.userProfileSettings).setOnClickListener{
            showMenu(view.findViewById(R.id.userProfileSettings))
        }
    }


    private fun callContact(remotePhoneNumber: String) {
        val callHandler = CallHandler.getInstance()
        callHandler.call(remotePhoneNumber, requireContext())
    }


    private fun showMenu(v: View) {
        PopupMenu(this.context, v).apply {
            setOnMenuItemClickListener(this@UserProfileFragment)
            inflate(R.menu.user_profile_menu)
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deletecontact -> {
                runBlocking {  userProfileFragmentViewModel.removeContact(profilePhoneNumber.text as String) }
                navController!!.navigate(
                    R.id.action_userProfileFragment_to_ProfileFragment)
                true
            }
            else -> false
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