package com.example.sub

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sub.session.CallHandler
import com.example.sub.transcription.TranscriptionClient
import com.example.sub.session.CallSession
import com.example.sub.session.SessionListener
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.pow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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

    private var callSession: CallSession? = null
    var firstName: String? = null
    var lastName: String? = null
    var phoneNr: String? = null

    private var navController: NavController? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_calling, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val microphoneHandler = MicrophoneHandler()
        val transcriptionclient = TranscriptionClient()
        var id = arguments?.getString("phone_nr")!!.toLong().mod(1000000000).toInt()
        var recordTimer = Timer()
        var answerTimer = Timer()
        var receivingTimer = Timer()
        var ownerIds = mutableListOf<Int>()
        var receivingIds = mutableListOf<Int>()
        var receivingSounds = mutableListOf<ByteArray>()
        var mediaPlayer : MediaPlayer
        var uri : Uri

        answerTimer.schedule(1000, 1000) {
            var removeIds = mutableListOf<Int>()
            for (textid in ownerIds){
                Log.e("Looking for answer", textid.toString())
                var answer = transcriptionclient.getAnswer(textid)
                if (answer == ""){
                    transcriptionclient.sendAnswer(textid, "owner")
                }
                else{
                    removeIds.add(textid)
                    getActivity()?.runOnUiThread(java.lang.Runnable{
                        updateUI(answer, "owner")
                    })
                    Log.e("answer", answer)
                }
            }
            for (textid in removeIds) {
                ownerIds.remove(textid)
            }
        }

        receivingTimer.schedule(1000, 1000) {
            var removeIds = mutableListOf<Int>()
            for (textid in receivingIds){
                Log.e("Looking for answer", textid.toString())
                var answer = transcriptionclient.getAnswer(textid)
                if (answer == ""){
                    transcriptionclient.sendAnswer(textid, "receiver")
                }
                else{
                    removeIds.add(textid)
                    getActivity()?.runOnUiThread(java.lang.Runnable{
                        updateUI(answer, "receiver")
                        playSound(receivingSounds[0])
                        receivingSounds.removeAt(0)
                    })
                    Log.e("answer", answer)
                }
            }
            for (textid in removeIds) {
                receivingIds.remove(textid)
            }
        }

        firstName = arguments?.getString("first_name")
        lastName = arguments?.getString("last_name")
        phoneNr = arguments?.getString("phone_nr")

        userName = view.findViewById(R.id.caller_name)
        userName.text = firstName
        navController = findNavController(view.findViewById(R.id.closeCall))
        view.findViewById<View>(R.id.closeCall).setOnClickListener {
            callSession?.hangUp()
            //closeCall()
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_calling)
        recyclerView.adapter = adapter



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
        val transcribeButton = view.findViewById<Button>(R.id.buttonTranscribe)

        transcribeButton.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("SetTextI18n")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {


                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        microphoneHandler.StartAudioRecording()
                        transcribeButton.text = "recording"


                        recordTimer.schedule(5000, 5000) {
                            if(microphoneHandler.getRecordingStatus()){
                                id = id.plus(1)
                                val bigbuff = microphoneHandler.StopAudioRecording()
                                microphoneHandler.StartAudioRecording()
                                transcriptionclient.sendSound(id, bigbuff)
                                transcriptionclient.sendAnswer(id, "owner")
                                ownerIds.add(id)
                                var idBytes = ByteBuffer.allocate(4).putInt(id).array();
                                Log.e("Id int ", id.toString())
                                Log.e("Id bytes", idBytes.toString())
                                callSession?.sendBytes(idBytes.plus(bigbuff))
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        recordTimer.cancel()
                        recordTimer.purge()
                        recordTimer = Timer()
                        id = id.plus(1)
                        transcribeButton.text = "press to record"
                        val bigbuff = microphoneHandler.StopAudioRecording()
                        transcriptionclient.sendSound(id, bigbuff)
                        transcriptionclient.sendAnswer(id, "owner")
                        ownerIds.add(id)
                        val idBytes = ByteBuffer.allocate(4).putInt(id).array();
                        Log.e("Id int ", id.toString())
                        Log.e("Id bytes", idBytes.toString())
                        callSession?.sendBytes(idBytes.plus(bigbuff))
                    }

                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        // Timer
        // TODO: place in a suitable place, timer should start when phone call starts, not when this fragment is created
        val simpleChronometer =
            view.findViewById(R.id.simpleChronometer) as Chronometer // initiate a chronometer
        simpleChronometer.start() // start a chronometer

        // Temporary. Initiate a call request to the contact
        callSession = CallHandler.getInstance().activeSession
        callSession?.addListener( SessionChangeHandler(receivingIds, receivingSounds) )
    }

    fun updateUI(message: String, ownerType: String){
        if (ownerType=="owner"){
            adapter.add(ChatToItem(message))
        }
        else if (ownerType=="receiver"){
            adapter.add(ChatFromItem(message))
        }
        if (adapter.itemCount > 2){
            adapter.removeGroupAtAdapterPosition(0)
        }
        adapter!!.notifyDataSetChanged()
    }

    fun playSound(sound: ByteArray){
    }


    inner class SessionChangeHandler(receivingIds: MutableList<Int>, sounds: MutableList<ByteArray>) : SessionListener {
        override fun onSessionEnded() {
            closeCall()
        }
        var receivingIds = receivingIds
        var sounds = sounds
        override fun onBytesMessage(bytes: ByteArray) {
            Log.e("Bytes received", bytes.toString())
            //Converting the first 4 bytes of the bytearray back to an int that will be used as id for transcription server
            var id = 256.toDouble().pow(3).toInt()*(bytes[0].toUByte().toInt())+256.toDouble().pow(2).toInt()*(bytes[1].toUByte().toInt())+256*(bytes[2].toUByte().toInt())+(bytes[3].toUByte().toInt())
            Log.e("Received id is", id.toString())
            //Add to the list of incoming transcriptions.
            receivingIds.add(receivingIds.size, id)
            //Since first 4 bytes is the manually attached id, dont copy thoose.
            sounds.add(sounds.size, bytes.copyOfRange(4, bytes.size))
        }
    }


    /**
     * Navigates back to the profile fragment.
     */
    private fun closeCall() {
        val bundle = Bundle()
        bundle.putString("first_name", firstName)
        bundle.putString("last_name", lastName)
        bundle.putString("phone_nr", phoneNr)

        // Navigate using global scope.
        GlobalScope.launch {
            try {
                navController?.navigate(R.id.action_callingFragment_to_userProfileFragment, bundle)
            } catch (e: Exception) {}
        }

    }

    companion object {
        fun newInstance(): CallingFragment {
            return CallingFragment()
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


