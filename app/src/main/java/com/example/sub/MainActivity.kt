package com.example.sub

import com.example.sub.SignalingClient
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
import com.example.sub.databinding.ActivityPermissionBinding
import com.example.sub.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import org.json.JSONObject
import kotlinx.coroutines.launch

import com.example.sub.session.SignalWebsocketListener
import okhttp3.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var layout: View
    private lateinit var binding: ActivityPermissionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        layout = binding.permissionLayout
        setContentView(R.layout.activity_main)
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

    @Serializable
    data class User(val Reason : String, val Email : String, val PhoneNumber : String, val Name: String, val Password: String)

    fun onClickSignalServer(view: View){
        // wss test
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            //.sslSocketFactory()
            .build()
        val request = Request.Builder()
            .url("ws://144.24.171.133:4000") // 'ws'
            .build()
        val wsListener = SignalWebsocketListener ()
        val webSocket = client.newWebSocket(request, wsListener) // this provide to make 'Open ws connection'


        /*
        val data = User("connect", "test@domain.com", "222333", "Edward Blom", "password123")
        val dataString = Json.encodeToString(data)
        println(dataString)
        val sc = SignalingClient()
        sc.sendCommand(dataString)
        */
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



