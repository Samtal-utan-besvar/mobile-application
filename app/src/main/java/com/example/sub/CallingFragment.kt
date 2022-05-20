package com.example.sub

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sub.session.CallHandler
import com.example.sub.session.CallSession
import com.example.sub.session.CallStatus
import com.example.sub.session.SessionListener
import com.example.sub.transcription.TranscriptionClient
import com.example.sub.transcription.TranscriptionListener
import com.example.sub.transcription.UuidUtils
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*
import kotlin.concurrent.schedule


/**
 * A simple [Fragment] subclass.
 * Use the [CallingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallingFragment : Fragment() {

    private var intBufferSize = 0
    private lateinit var bigbuff : ByteArray

    private lateinit var audioTrack: AudioTrack
    private var adapter = GroupieAdapter()
    private lateinit var userName : TextView

    private var callSession: CallSession? = null
    var firstName: String? = null
    var lastName: String? = null
    var phoneNr: String? = null

    private lateinit var microphoneHandler : MicrophoneHandler
    private lateinit var transcriptionclient : TranscriptionClient
    private lateinit var ownerIds : MutableList<String> //outgoing transcriptions id's
    private lateinit var receivingIds : MutableList<String>//incoming transcription id's
    private lateinit var receivingSounds : MutableMap<String, ByteArray> //All the sounds to be played
    private lateinit var recordTimer : Timer //used to split audio every 5 seconds

    private var navController: NavController? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_calling, container, false)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        microphoneHandler = MicrophoneHandler()
        transcriptionclient = TranscriptionClient(requireContext())
        transcriptionclient.transcriptionListeners.add(TranscriptionHandler())
        recordTimer = Timer() //used to split audio every 5 seconds

        ownerIds = mutableListOf<String>() //outgoing transcriptions id's
        receivingIds = mutableListOf<String>() //incoming transcription id's
        receivingSounds = mutableMapOf<String, ByteArray>() //All the sounds to be played


        firstName = arguments?.getString("first_name")
        lastName = arguments?.getString("last_name")
        phoneNr = arguments?.getString("phone_nr")

        userName = view.findViewById(R.id.caller_name)
        userName.text = firstName
        navController = findNavController(view.findViewById(R.id.closeCall))
        view.findViewById<View>(R.id.closeCall).setOnClickListener {

            //getFragmentManager()?.popBackStack()
            callSession?.hangUp()
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_calling)
        recyclerView.adapter = adapter


        /*
        // onClick for Speaker toggleButton
        val toggleButtonSilentMode: ToggleButton = view.findViewById(R.id.toggleButtonSilentMode)
        toggleButtonSilentMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Action when speaker is on
                Toast.makeText(activity, "Högtalare: PÅ", Toast.LENGTH_LONG).show()    // remove
            } else {
                // TODO: Action when speaker is off
                Toast.makeText(activity, "Högtalare: AV", Toast.LENGTH_LONG).show()    // remove
            }
        }


        // onClick for Mute toggleButton
        val toggleButtonMute: ToggleButton = view.findViewById(R.id.toggleButtonMute)
        toggleButtonMute.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Action when un-muted
                Toast.makeText(activity, "Mikrofon: PÅ", Toast.LENGTH_LONG).show()       // remove
            } else {
                // TODO: Action when muted
                Toast.makeText(activity, "Mikrofon: AV", Toast.LENGTH_LONG).show()          // remove
            }
        }

         */

        val transcribeButton = view.findViewById<RecordProgress>(R.id.buttonTranscribe)
        transcribeButton.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("SetTextI18n", "MissingPermission")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {

                        transcribeButton.start(5000)
                        sendRecordingNow("ja")
                        microphoneHandler.StartAudioRecording()

                        recordTimer.schedule(5000, 5000) {
                            if(microphoneHandler.getRecordingStatus()){
                                sendMiddleRecording()
                                transcribeButton.restart()
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        transcribeButton.stop()
                        sendRecordingNow("nej")
                        recordTimer.cancel()
                        recordTimer.purge()
                        recordTimer = Timer()

                        sendRecording()
                    }

                }

                return v?.onTouchEvent(event) ?: true
            }
        })

        // Timer
        val simpleChronometer =
            view.findViewById(R.id.simpleChronometer) as Chronometer // initiate a chronometer
        simpleChronometer.start() // start a chronometer

        // Setup listener for CallSession
        callSession = CallHandler.getInstance().activeSession
        callSession?.addListener( SessionChangeHandler(receivingIds, receivingSounds) )
    }


    /**
     * Sends the recorded audio stored in [bigbuff]
     */
    fun sendRecording() {
        bigbuff = microphoneHandler.StopAudioRecording()
        if (bigbuff.size > 6400) { // 1/5 of a second
            val id = UUID.randomUUID()
            transcriptionclient.sendSound(id.toString(), bigbuff)
            transcriptionclient.sendAnswer(id.toString(), "owner")
            ownerIds.add(id.toString())
            val idBytes = UuidUtils.asBytes(id)
            callSession?.sendBytes(idBytes.plus(bigbuff))
        }
    }

    fun sendMiddleRecording(){
        bigbuff = microphoneHandler.extractData()
        if (bigbuff.size > 6400) { // 1/5 of a second
            val id = UUID.randomUUID()
            transcriptionclient.sendSound(id.toString(), bigbuff)
            transcriptionclient.sendAnswer(id.toString(), "owner")
            ownerIds.add(id.toString())
            val idBytes = UuidUtils.asBytes(id)
            callSession?.sendBytes(idBytes.plus(bigbuff))
        }
    }


    /**
     * Handles answers from the transcription server.
     */
    inner class TranscriptionHandler : TranscriptionListener {

        override fun onTranscriptionComplete(id: String, text: String) {

            val isOwner = !receivingIds.contains(id)

            //update UI to show transcribed text.
            activity?.runOnUiThread {
                updateUI(
                    text,
                    isOwner
                )
            }

            // Play sound if not the owner of the sound.
            if (!isOwner) {
                playSound(receivingSounds[id]!!)
                receivingSounds.remove(id)
                receivingIds.remove(id)
            }
        }
    }


    fun playSound(buff: ByteArray){

        val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioAttributes.USAGE_MEDIA)
        intBufferSize = AudioRecord.getMinBufferSize(
                intRecordSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                        AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                )
                .setAudioFormat(
                        AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(16000)
                                .setChannelIndexMask(AudioFormat.CHANNEL_OUT_DEFAULT)
                                .build()
                )
                .setBufferSizeInBytes(intBufferSize)
                .build()
        audioTrack.playbackRate = 16000
        audioTrack.play()
        var count = 0;
        while (count < buff.size) {
            val written : Int = audioTrack.write(buff, count, buff.size);
            if (written <= 0) {
                break;
            }
            count += written
        }
        audioTrack.release()
    }


    fun sendRecordingNow(recording: String) {
        val message = JSONObject()
        message.put("Reason", "notifyRecording")
        message.put("Recording", recording)
        val msgList = listOf(message)
        callSession?.sendString(msgList.toString())
    }


    fun updateUI(message: String, isOwner: Boolean){
        //check for left or right side of call
        if (isOwner){
            adapter.add(ChatToItem(message))
        }
        else {
            adapter.add(ChatFromItem(message))
        }
        if (adapter.itemCount > 2){
            adapter.removeGroupAtAdapterPosition(0)
        }
        adapter!!.notifyDataSetChanged()
    }


    inner class SessionChangeHandler(var receivingIds: MutableList<String>,
                                     var sounds: MutableMap<String, ByteArray>
    ) : SessionListener {

        override fun onSessionEnded() {
            closeCall()
        }


        override fun onBytesMessage(bytes: ByteArray) {
            Log.e("Bytes received", bytes.toString())

            val idLength = 16
            val idBytes = bytes.copyOfRange(0, idLength)
            val id = UuidUtils.asUuid(idBytes).toString()
            Log.e("Received id is", id)

            //Add id to the list of incoming transcriptions.
            receivingIds.add(id)

            //Copy only audio and store it with the id as key.
            sounds[id] = bytes.copyOfRange(idLength, bytes.size)
            transcriptionclient.sendAnswer(id, "receiver")
        }


        override fun onStringMessage(message: String) {
            println("onStringMessage: ")
            var recording : String = ""
            var reason : String = ""
            val jsonArray = JSONTokener(message).nextValue() as JSONArray
            for (i in 0 until jsonArray.length()) {
                reason = jsonArray.getJSONObject(i).getString("Reason")
                recording = jsonArray.getJSONObject(i).getString("Recording")
            }

            activity?.runOnUiThread(Runnable {
                if (reason == "notifyRecording"){
                    if (recording == "ja"){
                        //view?.findViewById<View>(R.id.recordingBusy)?.visibility = View.VISIBLE
                        println("jag är här i ja")
                        view?.findViewById<View>(R.id.recordingAvailable)?.visibility = View.GONE
                    }
                    else{
                        view?.findViewById<View>(R.id.recordingAvailable)?.visibility = View.VISIBLE
                    }
                }
            })

        }
    }


    override fun onDestroy() {
        if (callSession?.getStatus() == CallStatus.IN_CALL) {
            callSession?.hangUp()
        }
        super.onDestroy()
    }


    /**
     * Navigates back to the profile fragment.
     */
    private fun closeCall() {
        ownerIds.clear()
        receivingIds.clear()
        receivingSounds.clear()
        transcriptionclient.close()
        microphoneHandler.close()
        val bundle = Bundle()
        bundle.putString("first_name", firstName)
        bundle.putString("last_name", lastName)
        bundle.putString("phone_nr", phoneNr)

        // Navigate using global scope.
        GlobalScope.launch {
            try {
                //getFragmentManager()?.popBackStackImmediate()
                navController?.navigate(R.id.userProfileFragment, bundle)
            } catch (e: Exception) {
                Log.d("error messagelalalala:", e.toString())

            }
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


