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
import java.util.*
import kotlin.concurrent.schedule


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
        var id = 80
        var recordTimer = Timer()
        var answerTimer = Timer()
        var textIds = mutableListOf<Int>()
        var uri : Uri



        answerTimer.schedule(1000, 1000) {
            var removeIds = mutableListOf<Int>()
            for (textid in textIds){
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
                textIds.remove(textid)
            }
        }

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

            callSession?.hangUp()

            navController?.navigate(R.id.action_callingFragment_to_userProfileFragment, bundle)



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

        view.findViewById<View>(R.id.playButton).setOnClickListener {
            //val music : MediaPlayer = MediaPlayer.create(activity, R.raw.sample)
            //music.start()
            playSound(bigbuff, audioTrack)
        }
        val testMp3 = File.createTempFile("test", "3gp", requireContext().cacheDir)
        val transcribeButton = view.findViewById<Button>(R.id.buttonTranscribe)
        transcribeButton.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("SetTextI18n", "MissingPermission")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        microphoneHandler.StartAudioRecording()
                        transcribeButton.text = "recording"
                        val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioAttributes.USAGE_MEDIA)
                        intBufferSize = AudioRecord.getMinBufferSize(
                                intRecordSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
                        )
                        /**audioRecord = AudioRecord(
                                MediaRecorder.AudioSource.MIC,
                                intRecordSampleRate,
                                AudioFormat.CHANNEL_IN_STEREO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                intBufferSize
                        )**/
                        audioTrack = AudioTrack.Builder()
                                .setAudioAttributes(
                                        AudioAttributes.Builder()
                                                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
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
                        //audioRecord.startRecording()

                        recordTimer.schedule(5000, 5000) {
                            if(microphoneHandler.recording.get()){
                                id+=1
                                val bigbuff = microphoneHandler.StopAudioRecording()
                                microphoneHandler.StartAudioRecording()
                                transcriptionclient.sendSound(id, bigbuff)
                                transcriptionclient.sendAnswer(id, "owner")
                                textIds.add(id)
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        recordTimer.cancel()
                        recordTimer.purge()
                        recordTimer = Timer()
                        id +=1
                        transcribeButton.text = "press to record"
                        bigbuff = microphoneHandler.StopAudioRecording()
                        transcriptionclient.sendSound(id, bigbuff)
                        transcriptionclient.sendAnswer(id, "owner")
                        textIds.add(id)
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
        callContact(phoneNr!!)
    }

    fun playSound(buff: ByteArray, at: AudioTrack){
        audioTrack.play()
        //audioTrack.write(bigbuff, 0, bigbuff.size)
        //
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



    fun updateUI(message: String, ownerType: String){
        if (ownerType=="owner"){
            adapter.add(ChatToItem(message))
        }
        else if (ownerType=="receiver"){
            adapter.add(ChatFromItem(message))
        }
        adapter!!.notifyDataSetChanged()
    }

    companion object {
        fun newInstance(): CallingFragment {
            return CallingFragment()
        }
    }

    // Temporary. Call contact based on phone number
    private fun callContact(remotePhoneNumber: String) {
        val callHandler = CallHandler.getInstance()
        callSession = callHandler.call(remotePhoneNumber, requireContext())
    }


    // MAX - FuNKTIONER ----------------------------

   /** private fun startRecording() {
        MediaRecorder()
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }**/




    // -------------------------------------------

    /**private fun playAssetSound(assetName: String) {
        try {
            val afd: AssetFileDescriptor = assets.openFd("sounds/$assetName.mp3")
            mMediaPlayer = MediaPlayer()
            mMediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mMediaPlayer.prepare()
            mMediaPlayer.start()
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        }
    }**/
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


