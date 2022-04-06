package com.example.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class profileFragment : Fragment(), contactListAdapter.ListItemClickListener {
    var navController: NavController? = null

    private lateinit var addContactBttn: View
    private lateinit var addContactText: View
    private lateinit var confirmContact: View
    private lateinit var contactName: TextView
    private lateinit var contactNumber: TextView
    private val contacts = ArrayList<User>()
    private val profileFragmentViewModel : ProfileFragmentViewModel by activityViewModels()

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
        contactName = view.findViewById(R.id.contactName)
        contactNumber = view.findViewById(R.id.contactNr)

        confirmContact.visibility = View.GONE
        contactName.visibility = View.GONE
        contactNumber.visibility = View.GONE
        addContactText.visibility = View.GONE

        profileFragmentViewModel.getUsers()

        var test = User("hej", "då", "01235460")
        contacts?.add(test)
        print(contacts)
        val recyclerView = view.findViewById<RecyclerView>(R.id.contactList)
        //val adapter = contacts?.let { contactListAdapter(it, this) }
        //val adapter = contacts?.let { contactListAdapter(it,this) }
        val adapter = contactListAdapter(contacts, this)
//        val layoutManager: RecyclerView.LayoutManager =
//            LinearLayoutManager(requireActivity().applicationContext)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        //recyclerView!!.setItemAnimator(DefaultItemAnimator())
        recyclerView.adapter = adapter
        if (adapter != null) {
            adapter.notifyDataSetChanged()
        }


        navController = findNavController(view.findViewById(R.id.AnnaKnappen))
        view.findViewById<View>(R.id.AnnaKnappen).setOnClickListener {
            navController!!.navigate(
                R.id.action_profileFragment_to_userProfile
            )
        }

        view.findViewById<View>(R.id.addContact).setOnClickListener {
            addContactBttn.visibility = View.GONE
            confirmContact.visibility = View.VISIBLE
            contactName.visibility = View.VISIBLE
            contactNumber.visibility = View.VISIBLE
            addContactText.visibility = View.VISIBLE
        }

        view.findViewById<View>(R.id.confirmContact).setOnClickListener {
            addContactBttn.visibility = View.VISIBLE
            confirmContact.visibility = View.GONE
            addContactText.visibility = View.GONE

            var newCont = User(contactName.text.toString(), "dådådå", contactNumber.text.toString())
            contacts.add(newCont)
            if (adapter != null) {
                adapter.notifyDataSetChanged()
            }
            contactName.text = ""
            contactNumber.text = ""
            contactName.visibility = View.GONE
            contactNumber.visibility = View.GONE

        }
    }

    override fun onListItemClick(position: Int) {
        System.out.print(position)
    }
}
