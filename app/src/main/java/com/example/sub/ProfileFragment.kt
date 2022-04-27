package com.example.sub

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sub.data.LoggedInUser
import kotlinx.coroutines.runBlocking

/** The ProfileFragment which shows the "Homescreen" and the Contactlist of the
 * specified user
 */
class ProfileFragment : Fragment(), contactListAdapter.ListItemClickListener {
    var navController: NavController? = null

    private lateinit var addContactBttn: View
    private lateinit var addContactText: View
    private lateinit var confirmContact: View
    private lateinit var contactFirstName: TextView
    private lateinit var contactLastName: TextView
    private lateinit var contactNumber: TextView
    private lateinit var contactList: RecyclerView
    private lateinit var contactGroup: Group
    private lateinit var loggedInUser: LoggedInUser
    private var contacts: MutableList<User> = ArrayList()
    private val profileFragmentViewModel : ProfileFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    /** Runs the necessary functions when running Profilefragment, maintains connection with
     * ProfileFragmentViewModel aswell
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        loggedInUser = (activity as MainActivity?)!!.getActiveUser()
        runBlocking {profileFragmentViewModel.setUserToken(getUserToken())}
        addContactBttn = view.findViewById(R.id.addContact)
        addContactText = view.findViewById(R.id.addContactText)
        confirmContact = view.findViewById(R.id.confirmContact)
        contactFirstName = view.findViewById(R.id.contactFirstName)
        contactLastName = view.findViewById(R.id.contactLastName)
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
            (activity as MainActivity?)!!.startLoginActivity()
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

    /** Return the displayName from LoggedInUser object **/
    private fun getUserName(): String? {
        return loggedInUser.firstName + " " + loggedInUser.lastName
    }

    /** Return the JWT Token from LoggedInUser object **/
    private fun getUserToken(): String {
        return loggedInUser.userToken.toString()
    }

    companion object {
        fun newInstance(): CallingFragment {
            return CallingFragment()
        }
    }
}
