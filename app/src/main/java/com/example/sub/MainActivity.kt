package com.example.sub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sub.databinding.ActivityPermissionBinding
import com.example.sub.session.CallHandler
import com.example.sub.session.ClientFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import org.json.JSONObject
import kotlinx.coroutines.launch

import okhttp3.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var layout: View
    private lateinit var binding: ActivityPermissionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        //val view = binding.root
        layout = binding.permissionLayout
        setContentView(R.layout.activity_main)
        ClientFactory.getCallHandler()

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

    // Test Button
    fun onClickSignalServer(view: View){
        //val websocketClient = SignalingClient()
        //websocketClient.connect()
        val sc1 = ClientFactory.getSignalClient()
        val sc2 = ClientFactory.getSignalClient()
        Log.d("", sc1.toString())
        Log.d("", sc2.toString())
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