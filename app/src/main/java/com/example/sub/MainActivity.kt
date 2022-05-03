package com.example.sub

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.sub.data.LoggedInUser
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
import com.example.sub.ui.login.LoginActivity

class MainActivity : AppCompatActivity(), CallReceivedListener {
    private lateinit var layout: View
    private lateinit var binding: ActivityPermissionBinding
    private lateinit var loggedInUser: LoggedInUser
    private lateinit var contactList: MutableList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        layout = binding.permissionLayout
        setContentView(R.layout.activity_main)
        loggedInUser = intent.getSerializableExtra("loggedInUser") as LoggedInUser

        setUpWebRTC()

        // asks for permission to record audio
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
        }
    }


    fun getContactList(): MutableList<User> {
        return contactList
    }

    fun setContactList(contactList_: MutableList<User>) {
        contactList = contactList_
    }


    /**
     * Sets up webRTC and signal client.
     */
    private fun setUpWebRTC() {

        val token = loggedInUser.userToken!!
        val localPhoneNumber = loggedInUser.phoneNumber!!

        // Crucial part.
        SignalClient.connect(token)
        CallHandler.initInstance(SignalClient, localPhoneNumber)
        CallHandler.getInstance().callReceivedListeners.add( this )
    }


    override fun onDestroy() {
        CallHandler.getInstance().callReceivedListeners.remove(this)
        SignalClient.close()

        super.onDestroy()
    }


    /**
     * Sets up what happens when someone calls.
     */
    override fun onCallReceived(callSession: CallSession) {

        if (!supportFragmentManager.isDestroyed) {
            val remotePhoneNumber = callSession.remotePhoneNumber
            val user = getContactList().stream().filter { user ->
                user.number == remotePhoneNumber
            }.findFirst().orElse(null)

            val displayName = if (user?.firstName == null)
                remotePhoneNumber else user.firstName!!

            val callDialog = CallDialog(displayName)

            callDialog.setOnAnswer {
                callSession.accept(applicationContext)
                val navController = callDialog.findNavController()

                val bundle = Bundle()
                bundle.putString("first_name", user.firstName)
                bundle.putString("last_name", user.lastName)
                bundle.putString("phone_nr", user.number)

                navController.navigate(R.id.callingFragment, bundle)
                //TODO: go to CallingFragment
            }

            callDialog.setOnDeny {
                callSession.deny()
            }

            callDialog.show(supportFragmentManager, "callDialog")
        }

    }


    /**
     * Starts LoginActivity and finish MainActivity when logout button is pressed.
     */
    fun startLoginActivity() {
        let {
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
    fun getActiveUser(): LoggedInUser {
        return loggedInUser
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {    // if permission denied
                    Log.d("debug", "PERMISSION DENIED")
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder.setMessage(R.string.mic_required)
                        .setCancelable(false)
                        .setPositiveButton(R.string.open_settings) { _, _ ->
                            val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            i.addCategory(Intent.CATEGORY_DEFAULT)
                            i.data = Uri.parse("package:$packageName")
                            startActivity(i)
                        }
                        .setNegativeButton(R.string.exit_app) { _, _ ->
                            finish()
                        }

                    // create dialog box
                    val alert = dialogBuilder.create()
                    alert.setTitle(R.string.permissions)
                    alert.show()
                }
                return
            }
        }
    }

}