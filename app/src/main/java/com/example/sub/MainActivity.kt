package com.example.sub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sub.data.LoggedInUser
import com.example.sub.databinding.ActivityPermissionBinding
import com.example.sub.session.CallHandler
import com.example.sub.session.CallReceivedListener
import com.example.sub.session.CallSession
import com.example.sub.signal.SignalClient
import com.example.sub.signal.TOKEN1
import com.example.sub.signal.TOKEN2
import com.example.sub.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var layout: View
    private lateinit var binding: ActivityPermissionBinding
    private lateinit var loggedInUser: LoggedInUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        layout = binding.permissionLayout
        setContentView(R.layout.activity_main)
        loggedInUser = intent.getSerializableExtra("loggedInUser") as LoggedInUser

        setUpWebRTC()
    }


    /**
     * Sets up webRTC and signal client.
     */
    private fun setUpWebRTC() {

        //region Temporary solution.
        val token = if (android.os.Build.VERSION.SDK_INT == 30) TOKEN1 else TOKEN2

        val phoneNumber1 = "3333333333"
        val phoneNumber2 = "4444444444"

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

    /**
     * Starts LoginActivity and finish MainActivity when logout button is pressed.
     */
    fun startLoginActivity() {
        let{
            val intent = Intent(it, LoginActivity::class.java)
            intent.putExtra("logout", true)
            it.startActivity(intent)
        }
        finish()
    }

    /**
     * Return LoggedInUser object.
     *
     * @see com.example.sub.data.LoggedInUser for more information about accessible data.
     *
     * e.g.: to access phoneNumber in a fragment:
     * val phoneNumber = (activity as MainActivity?)!!.getActiveUser().phoneNumber
     */
    fun getActiveUser() : LoggedInUser {
        return loggedInUser
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