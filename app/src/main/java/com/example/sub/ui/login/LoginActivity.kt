package com.example.sub.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sub.MainActivity

import com.example.sub.R

const val LOGIN_DISABLED = false     // for debugging purposes

class LoginActivity : AppCompatActivity() {

    var navController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        println("HJEHEJEHEHJEHJEHJEHJEHJEHJEHJEHJEHJHJE")
        // Skips the login fragment, only for debugging
        if (LOGIN_DISABLED) {
            startMainActivity()
        }
    }

    /**
     * Starts mainActivity and finish the LoginActivity.
     */
    fun startMainActivity() {

        //let{

        //    val intent = Intent(it, MainActivity::class.java)
       //     it.startActivity(intent)
        //}
        //println("HGDAWIDHAWILDHLIAWDHLIAwdHLIAdwLHIKLDHAWILHIDAWHLIDAWHLIADKWhlIKDAWLHIK")
        //finish()
    }
}
