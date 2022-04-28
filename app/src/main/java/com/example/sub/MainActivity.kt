package com.example.sub

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sub.databinding.ActivityPermissionBinding
import com.example.sub.transcription.TranscriptionClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import java.util.*
import com.example.sub.session.CallHandler
import com.example.sub.session.CallReceivedListener
import com.example.sub.session.CallSession
import com.example.sub.signal.SignalClient
import com.example.sub.signal.TOKEN1
import com.example.sub.signal.TOKEN2
import com.example.sub.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var layout: View
    private lateinit var binding: ActivityPermissionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        //val view = binding.root
        layout = binding.permissionLayout
        setContentView(R.layout.activity_main)

        setUpWebRTC()
    }


    /**
     * Sets up webRTC and signal client.
     */
    private fun setUpWebRTC() {

        //region Temporary solution.
        val token = if (android.os.Build.VERSION.SDK_INT == 30) TOKEN1 else TOKEN2


        val phoneNumber1 = "0933503271"
        val phoneNumber2 = "0933703271"

        val localPhoneNumber: String

        if (android.os.Build.VERSION.SDK_INT == 30) {
            localPhoneNumber = phoneNumber1
        } else {
            localPhoneNumber = phoneNumber2
        }

        //endregion

        // Crucial part.
        SignalClient.connect(token)
        CallHandler.initInstance(SignalClient, localPhoneNumber)

        CallHandler.getInstance().callReceivedListeners.add( CallListener() )
    }


    // Sets up what happens when someone calls.
    private inner class CallListener : CallReceivedListener {
        override fun onCallReceived(callSession: CallSession) {
            val callDialog = CallDialog(callSession)
            callDialog.show(supportFragmentManager, "callDialog")

        }
    }


    fun startLoginActivity() {
        let{
            val intent = Intent(it, LoginActivity::class.java)
            it.startActivity(intent)
        }
        finish()
    }


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

    fun onClickRequestPermission(view: View) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED -> {
                layout.showSnackbar(
                    view, getString(R.string.permission_granted),
                    Snackbar.LENGTH_INDEFINITE, null
                ) {}
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
            -> {
                layout.showSnackbar(
                    view, getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

}

fun View.showSnackbar(
    view: View,
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this)
        }.show()
    } else {
        snackbar.show()
    }
}