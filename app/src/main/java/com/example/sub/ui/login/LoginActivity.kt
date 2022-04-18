package com.example.sub.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sub.MainActivity

import com.example.sub.R

const val LOGIN_DISABLED = false     // for debugging purposes

class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Skips the login fragment, only for debugging
        if (LOGIN_DISABLED) {
            startMainActivity()
        }
    }

    /**
     * Starts mainActivity and finish the LoginActivity.
     */
    fun startMainActivity() {
        let{
            val intent = Intent(it, MainActivity::class.java)
            it.startActivity(intent)
        }
        finish()
    }
}
