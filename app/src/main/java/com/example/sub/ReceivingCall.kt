package com.example.sub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sub.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReceivingCall.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReceivingCall : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receiving_call, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var navController: NavController? = null
        val firstName = arguments?.getString("first_name")
        val lastName = arguments?.getString("last_name")
        val phoneNr = arguments?.getString("phone_nr")

        navController = Navigation.findNavController(view.findViewById(R.id.callButtonReceivingCall))
        view.findViewById<View>(R.id.callButtonReceivingCall).setOnClickListener {
            val bundle = Bundle()
            bundle.putString("first_name", firstName)
            bundle.putString("last_name", lastName)
            bundle.putString("phone_nr", phoneNr)
            navController?.navigate(R.id.action_receivingCall_to_callingFragment, bundle)

        }
        navController = Navigation.findNavController(view.findViewById(R.id.refuseCallReceivingCall))
        view.findViewById<View>(R.id.refuseCallReceivingCall).setOnClickListener {
            val bundle = Bundle()
            bundle.putString("first_name", firstName)
            bundle.putString("last_name", lastName)
            bundle.putString("phone_nr", phoneNr)
            navController?.navigate(R.id.action_receivingCall_to_profileFragment, bundle)

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReceivingCall.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReceivingCall().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}