package com.example.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sub.session.CallHandler
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CallingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallingFragment : Fragment() {

    private var adapter = GroupieAdapter()
    private lateinit var userName : TextView



    private var navController: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_calling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val firstName = arguments?.getString("first_name")
        val lastName = arguments?.getString("last_name")
        val phoneNr = arguments?.getString("phone_nr")

        userName = view.findViewById(R.id.caller_name)
        userName.text = firstName
        navController = findNavController(view.findViewById(R.id.closeCall))
        view.findViewById<View>(R.id.closeCall).setOnClickListener {
            val bundle = Bundle()
            bundle.putString("first_name", firstName)
            bundle.putString("last_name", lastName)
            bundle.putString("phone_nr", phoneNr)

            // TODO: Action when close call, disconnect call from server??

            navController?.navigate(R.id.action_callingFragment_to_userProfileFragment, bundle)



        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_calling)
        recyclerView.adapter = adapter
        adapter.add(ChatFromItem("blablablablabla"))
        adapter.add(ChatToItem("hejhejhejhejhejhejhejhejhejehj"))
        adapter.add(ChatFromItem("blablablablabla"))
        adapter.add(ChatToItem("hejhejhejhejhejhejhejhejhejehj"))
        adapter.add(ChatFromItem("blablablablabla"))
        adapter.add(ChatToItem("hejhejhejhejhejhejhejhejhejehj"))
        adapter.add(ChatFromItem("blablablablabla"))
        adapter.add(ChatToItem("hejhejhejhejhejhejhejhejhejehj"))
        adapter.add(ChatFromItem("blablablablabla"))
        adapter.add(ChatToItem("hejhejhejhejhejhejhejhejhejehj"))
        adapter.add(ChatFromItem("blablablablabla"))
        adapter.add(ChatToItem("hejhejhejhejhejhejhejhejhejehj"))
        adapter.add(ChatFromItem("blablablablabla"))
        adapter.add(ChatToItem("hejhejhejhejhejhejhejhejhejehj"))
        adapter.add(ChatToItem("hejhejhejhejhejhejhejhejhejehj"))



        // onClick for Speaker toggleButton
        val toggleButtonSilentMode: ToggleButton = view.findViewById(R.id.toggleButtonSilentMode)
        toggleButtonSilentMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Action when speaker is on
                Toast.makeText(activity, "speaker on", Toast.LENGTH_LONG).show()    // remove
            } else {
                // TODO: Action when speaker is off
                Toast.makeText(activity, "speaker off", Toast.LENGTH_LONG).show()    // remove
            }
        }

        // onClick for Mute toggleButton
        val toggleButtonMute: ToggleButton = view.findViewById(R.id.toggleButtonMute)
        toggleButtonMute.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Action when un-muted
                Toast.makeText(activity, "un-mute", Toast.LENGTH_LONG).show()       // remove
            } else {
                // TODO: Action when muted
                Toast.makeText(activity, "mute", Toast.LENGTH_LONG).show()          // remove
            }
        }

        // Timer
        // TODO: place in a suitable place, timer should start when phone call starts, not when this fragment is created
        val simpleChronometer =
            view.findViewById(R.id.simpleChronometer) as Chronometer // initiate a chronometer
        simpleChronometer.start() // start a chronometer

        // Temporary. Initiate a call request to the contact
        callContact()
    }

    companion object {
        fun newInstance(): CallingFragment {
            return CallingFragment()
        }
    }

    // Temporary. Call contact based on phone number
    fun callContact() {
        val phoneNumber1 = "0933503271"
        val phoneNumber2 = "0933703271"

        val callHandler = CallHandler.getInstance()

        if (android.os.Build.VERSION.SDK_INT == 30) {
            callHandler.call(phoneNumber2, requireContext())
        } else {
            callHandler.call(phoneNumber1, requireContext())
        }
    }
}

class ChatFromItem(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.text_view_from).text = text

    }

    override fun getLayout() = R.layout.chat_from_row
}
class ChatToItem(val text:String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.text_view_to).text = text
    }

    override fun getLayout() = R.layout.chat_to_row
}


