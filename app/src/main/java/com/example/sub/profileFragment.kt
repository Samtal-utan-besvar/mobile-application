package com.example.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController

/**
 * A simple [Fragment] subclass.
 * Use the [profileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class profileFragment : Fragment() {
    var navController: NavController? = null
    private lateinit var addContactBttn: View
    private lateinit var addContactText: View
    private lateinit var confirmContact: View
    private lateinit var contactName: View
    private lateinit var contactNumber: View

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
            contactName.visibility = View.GONE
            contactNumber.visibility = View.GONE
            addContactText.visibility = View.GONE


        }


    }

    companion object {
        fun newInstance(): callingFragment {
            return callingFragment()
        }
    }
}
