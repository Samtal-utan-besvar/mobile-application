
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.example.sub.ui.login.LoginViewModel
import com.example.sub.data.LoggedInUser
import com.example.sub.ui.login.LoginViewModelFactory



class ProfileFragment : Fragment(), contactListAdapter.ListItemClickListener {
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
    /**
     * A simple [Fragment] subclass.
     * Use the [ProfileFragment.newInstance] factory method to
     * create an instance of this fragment.
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

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

        navController = findNavController(view.findViewById(R.id.AnnaKnappen))
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(context))[LoginViewModel::class.java]
        navController = findNavController(view)
        view.findViewById<View>(R.id.AnnaKnappen).setOnClickListener {
            navController!!.navigate(
                R.id.action_profileFragment_to_userProfile
            )
        }

        view.findViewById<View>(R.id.addContact).setOnClickListener {
            contactGroup.visibility = View.VISIBLE
            contactList.visibility = View.GONE
        }
            view.findViewById<View>(R.id.logout).setOnClickListener {
                loginViewModel.loginRepository.logout()
                (activity as MainActivity?)!!.startLoginActivity()
            }

            view.findViewById<View>(R.id.confirmContact).setOnClickListener {
                if (adapter != null) {
                    adapter.notifyDataSetChanged()
                }
                contactFirstName.text = ""
                contactLastName.text = ""
                contactNumber.text = ""
                contactGroup.visibility = View.GONE
                contactList.visibility = View.VISIBLE
            }
            Log.d("myDebug", "getUserToken(): " + getUserToken())
        }

        private fun getUserName(): String? {
            return loginViewModel.loginRepository.user?.displayName
        }

        override fun onListItemClick(position: Int) {
            println(position)
            val bundle = Bundle()
            val gson = Gson()
            bundle.putSerializable("user", gson.toJson(contacts[position]))
            println(bundle)
        }

        private fun getUserToken(): String? {
            return loginViewModel.loginRepository.user?.userToken
        }

            companion object {
                fun newInstance(): CallingFragment {
                    return CallingFragment()
                }
            }
        }
