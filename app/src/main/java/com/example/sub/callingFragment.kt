package com.example.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [callingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class callingFragment : Fragment() {
    private var navController: NavController? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_calling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = findNavController(view.findViewById(R.id.closeCall))
        view.findViewById<View>(R.id.closeCall).setOnClickListener {
            navController!!.navigate(
                R.id.action_callingFragment_to_userProfile
            )
        }
    }

    companion object {
        fun newInstance(): callingFragment {
            return callingFragment()
        }
    }
}