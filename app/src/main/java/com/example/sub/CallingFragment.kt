package com.example.sub

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sub.session.CallHandler
import com.example.sub.session.CallSession
import com.example.sub.transcription.TranscriptionClient
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.io.*
import com.example.sub.session.SessionListener
import com.example.sub.signal.SignalClient.send
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.pow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.util.concurrent.atomic.AtomicBoolean


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



    private var intBufferSize = 0
    private lateinit var bigbuff : ByteArray

    private lateinit var audioRecord: AudioRecord
    private lateinit var audioTrack: AudioTrack
    private var adapter = GroupieAdapter()
    private lateinit var userName : TextView

    private var callSession: CallSession? = null
    var firstName: String? = null
    var lastName: String? = null
    var phoneNr: String? = null

    private lateinit var microphoneHandler : MicrophoneHandler
    private lateinit var transcriptionclient : TranscriptionClient
    private lateinit var ownerIds : MutableList<Int> //outgoing transcriptions id's
    private lateinit var receivingIds : MutableList<Int>//incoming transcription id's
    private lateinit var receivingSounds : MutableList<ByteArray> //All the sounds to be played
    private lateinit var recordTimer : Timer //used to split audio every 5 seconds
    private lateinit var answerTimer : Timer //used to retrieve locally recorded transcriptions from server
    private lateinit var receivingTimer : Timer //used to retrieve transcriptions of incoming audio from server

    private var playingSound = AtomicBoolean(false)

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
        transcriptionclient = TranscriptionClient()
        var id = arguments?.getString("phone_nr")!!.toLong().mod(1000000000).toInt() //casts the phone number so that fits in the first 4 bytes of sound bytearray
        recordTimer = Timer() //used to split audio every 5 seconds
        answerTimer = Timer() //used to retrieve locally recorded transcriptions from server
        receivingTimer = Timer() //used to retrieve transcriptions of incoming audio from server
        ownerIds = mutableListOf<Int>() //outgoing transcriptions id's
        receivingIds = mutableListOf<Int>() //incoming transcription id's
        receivingSounds = mutableListOf<ByteArray>() //All the sounds to be played
        playingSound.set(false)
        var mediaPlayer : MediaPlayer
        var uri : Uri



        answerTimer.schedule(500, 500) {
            var removeIds = mutableListOf<Int>()
            for (textid in ownerIds){
                Log.e("Looking for answer", textid.toString())
                var answer = transcriptionclient.getAnswer(textid)
                if (answer == ""){ //ping server for new answer if there is no previously retrieved one
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

        receivingTimer.schedule(500, 500) {
            var removeIds = mutableListOf<Int>()
            for (textid in receivingIds){
                Log.e("Looking for answer", textid.toString())
                var answer = transcriptionclient.getAnswer(textid)
                if (answer == ""){
                    transcriptionclient.sendAnswer(textid, "receiver")
                }
                else{
                    if (!playingSound.get()) {
                        removeIds.add(textid)
                        getActivity()?.runOnUiThread(java.lang.Runnable {
                            playingSound.set(true)
                            updateUI(
                                answer,
                                "receiver"
                            ) //update UI and play sound at the same time for incoming data
                            playSound(receivingSounds[0])
                            receivingSounds.removeAt(0)
                            playingSound.set(false)
                        })
                        Log.e("answer", answer)
                    }
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

        val testMp3 = File.createTempFile("test", "3gp", requireContext().cacheDir)
        val transcribeButton = view.findViewById<Button>(R.id.buttonTranscribe)
        transcribeButton.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("SetTextI18n", "MissingPermission")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        sendRecordingNow("ja")
                        microphoneHandler.StartAudioRecording()
                        transcribeButton.text = "Spelar in"
                        /**audioRecord = AudioRecord(
                                MediaRecorder.AudioSource.MIC,
                                intRecordSampleRate,
                                AudioFormat.CHANNEL_IN_STEREO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                intBufferSize
                        )**/

                        //audioRecord.startRecording()

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
                        sendRecordingNow("nej")
                        recordTimer.cancel()
                        recordTimer.purge()
                        recordTimer = Timer()

                        transcribeButton.text = "Starta Transkribering"
                        bigbuff = microphoneHandler.StopAudioRecording()
                        if (bigbuff.size > 6400) { // 1/5 of a second 
                            id = id.plus(1)
                            transcriptionclient.sendSound(id, bigbuff)
                            transcriptionclient.sendAnswer(id, "owner")
                            ownerIds.add(id)
                            val idBytes = ByteBuffer.allocate(4).putInt(id)
                                .array() //put the id in the first 4 bytes of the sound
                            callSession?.sendBytes(idBytes.plus(bigbuff))
                        }
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
            val written : Int = audioTrack.write(buff, count, buff.size)
            if (written <= 0) {
                break
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

    fun updateUI(message: String, ownerType: String){
        //check for left or right side of call
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


