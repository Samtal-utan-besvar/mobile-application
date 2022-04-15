package com.example.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


class ContactListFragment : Fragment(), contactListAdapter.ListItemClickListener {
    var navController: NavController? = null

    private lateinit var contactText   : TextView
    private lateinit var addContactBttn: View
    private lateinit var addContactText: View
    private lateinit var confirmContact: View
    private lateinit var contactFirstName: TextView
    private lateinit var contactLastName: TextView
    private lateinit var contactNumber: TextView
    private lateinit var contactList: RecyclerView
    private lateinit var contactGroup: Group
    private val contacts = ArrayList<User>()
    private val profileFragmentViewModel : ProfileFragmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_contact_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val adapter = GroupAdapter<GroupieViewHolder>()


        addContactText = view.findViewById(R.id.addContactText)
        confirmContact = view.findViewById(R.id.confirmContact)
        contactFirstName = view.findViewById(R.id.contactFirstName)
        contactLastName = view.findViewById(R.id.contactLastName)
        contactNumber = view.findViewById(R.id.contactNr)
        contactGroup = view.findViewById(R.id.addContactGroup)
        contactList = view.findViewById(R.id.contactList)
        contactGroup.visibility = View.GONE
        contactList.layoutManager = LinearLayoutManager(activity)
        contactList.adapter = adapter
        //confirmContact.visibility = View.GONE
        //contactName.visibility = View.GONE
        //contactNumber.visibility = View.GONE
        //addContactText.visibility = View.GONE

        profileFragmentViewModel.getUsers()

        var test = User("hej", "då", "01235460")
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))
        adapter.add(UserItem(test))



        print(contacts)
        if (adapter != null) {
            adapter.notifyDataSetChanged()
        }

        contactList.adapter = adapter




        navController = findNavController(view.findViewById(R.id.AnnaKnappen))
        view.findViewById<View>(R.id.AnnaKnappen).setOnClickListener {
            navController!!.navigate(
                R.id.action_profileFragment_to_userProfile
            )
        }

        view.findViewById<View>(R.id.addContact).setOnClickListener {
            contactGroup.visibility = View.VISIBLE
            contactList.visibility = View.GONE
            //addContactBttn.visibility = View.GONE
            //confirmContact.visibility = View.VISIBLE
            //contactName.visibility = View.VISIBLE
            //contactNumber.visibility = View.VISIBLE
            //addContactText.visibility = View.VISIBLE

        }

        view.findViewById<View>(R.id.confirmContact).setOnClickListener {
            //addContactBttn.visibility = View.VISIBLE

            //confirmContact.visibility = View.GONE
            //addContactText.visibility = View.GONE

            var newCont = User(contactFirstName.text.toString(), contactLastName.text.toString(), contactNumber.text.toString())
            contacts.add(newCont)
            if (adapter != null) {
                adapter.notifyDataSetChanged()
            }
            contactFirstName.text = ""
            contactLastName.text = ""
            contactNumber.text = ""
            contactGroup.visibility = View.GONE
            contactList.visibility = View.VISIBLE
            //contactName.visibility = View.GONE
            //contactNumber.visibility = View.GONE

        }
    }

    override fun onListItemClick(position: Int) {
        println(position)
        val bundle = Bundle()
        val gson = Gson()
        bundle.putSerializable("user", gson.toJson(contacts[position]))
        println(bundle)
    }
}


class UserItem(val user: User): Item<GroupieViewHolder>() {


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.text_view_new_message).text = user.firstName


    }


    override fun getLayout(): Int {
        return R.layout.user_row_new_messages
        //viewHolder.itemView.findViewById<TextView>(R.id.user_row_new_messages).text = user.firstName

    }
}
