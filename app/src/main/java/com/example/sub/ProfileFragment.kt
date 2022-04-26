package com.example.sub

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*

import com.google.android.material.textfield.TextInputLayout

import androidx.constraintlayout.widget.Group
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sub.ui.login.LoginViewModel
import com.example.sub.ui.login.LoginViewModelFactory
import kotlinx.coroutines.runBlocking

/** The ProfileFragment which shows the "Homescreen" and the Contactlist of the
 * specified user
 */
class ProfileFragment : Fragment(), contactListAdapter.ListItemClickListener, PopupMenu.OnMenuItemClickListener {
    var navController: NavController? = null

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var addContactBttn: View
    private lateinit var addContactText: View
    private lateinit var confirmContact: View
    private lateinit var contactFirstName: TextView
    private lateinit var contactLastName: TextView
    private lateinit var contactNumber: TextView
    private lateinit var contactList: RecyclerView
    private lateinit var contactGroup: Group
    private var contacts: MutableList<User> = ArrayList()
    private val profileFragmentViewModel : ProfileFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    /** Runs the necessary functions when running Profilefragment, maintains connection with
     * ProfileFragmentViewModel aswell
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(context))[LoginViewModel::class.java]
        runBlocking {  loginViewModel.getUser()?.userToken?.let { profileFragmentViewModel.setUserToken(it) }}
        addContactBttn = view.findViewById(R.id.addContact)
        addContactText = view.findViewById(R.id.addContactText)
        confirmContact = view.findViewById(R.id.confirmContact)
        //contactFirstName = view.findViewById(R.id.contactFirstName)
        //contactLastName = view.findViewById(R.id.contactLastName)
        contactNumber = view.findViewById(R.id.contactNr)
        contactGroup = view.findViewById(R.id.addContactGroup)
        contactList = view.findViewById(R.id.contactList)
        contactGroup.visibility = View.GONE
        profileFragmentViewModel.getUsers()

        val recyclerView = view.findViewById<RecyclerView>(R.id.contactList)
        val adapter = contactListAdapter(contacts, this)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        if (adapter != null) {
            adapter.notifyDataSetChanged()
        }
        profileFragmentViewModel.getUsers().observe(viewLifecycleOwner,
            Observer<List<User>> { strings ->
                adapter.changeDataSet(strings)
                adapter.notifyDataSetChanged()
                contacts = strings as MutableList<User>
            })
        navController = findNavController(view)
        view.findViewById<View>(R.id.addContact).setOnClickListener {
            contactGroup.visibility = View.VISIBLE
            contactList.visibility = View.GONE
        }
        view.findViewById<View>(R.id.logout).setOnClickListener {
            loginViewModel.loginRepository.logout()
            //(activity as MainActivity?)!!.startLoginActivity()
        }

        view.findViewById<View>(R.id.confirmContact).setOnClickListener {
            if (adapter != null) {
                adapter.notifyDataSetChanged()
            }

            //view.findViewById<Button>(R.id.buttonPerm).setOnClickListener{
            //    Log.d("blabla", "BLABLABLABL")
            //}

            runBlocking {  profileFragmentViewModel.addContact(contactNumber.text.toString())}
            contactFirstName.text = ""
            contactLastName.text = ""
            contactNumber.text = ""
            contactGroup.visibility = View.GONE
            contactList.visibility = View.VISIBLE
        }
        Log.d("myDebug", "getUserToken(): " + getUserToken())

        view.findViewById<View>(R.id.settingsButton).setOnClickListener{
            showMenu(view.findViewById(R.id.settingsButton))
        }
        /**val spinner: Spinner = view.findViewById<View>(R.id.spinner) as Spinner
        this.context?.let {
            ArrayAdapter.createFromResource(it, R.array.logout_array, android.R.layout.simple_spinner_item).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }**/
    }
    private fun showMenu(v: View) {
        PopupMenu(this.context, v).apply {
            setOnMenuItemClickListener(this@ProfileFragment)
            inflate(R.menu.logout_menu)
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        println(R.id.loggaut)
        println("itemid: ")
        println(item.itemId)
        return when (item.itemId) {
            R.id.loggaut -> {
                loginViewModel.loginRepository.logout()
                true
            }
            else -> false
        }
    }

    /**
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val inflater = MenuInflater(this.context)
        inflater.inflate(R.menu.logout_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.ProfileFragment){
            println("HOPPLA POLLE")
            return true
        }
        return NavigationUI.onNavDestinationSelected(item!!,
            requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }**/

    /** Fetches the username from the LoginViewModel class **/
    private fun getUserName(): String? {
        return loginViewModel.loginRepository.user?.displayName
    }

    /** Function used when a contact is clicked on,
    changes fragment to that contact and sends the necessary information **/
    override fun onListItemClick(position: Int) {
        val user : User = contacts[position]
        val bundle : Bundle = Bundle()
        bundle.putString("first_name", user.firstName)
        bundle.putString("last_name", user.lastName)
        bundle.putString("phone_nr", user.number)
        navController?.navigate(R.id.action_ProfileFragment_to_userProfileFragment, bundle)
    }

    /** Fetches the JWT UserToken from the LoginViewModel class **/
    private fun getUserToken(): String? {
        return loginViewModel.loginRepository.user?.userToken
    }

    companion object {
        fun newInstance(): CallingFragment {
            return CallingFragment()
        }
    }
}
