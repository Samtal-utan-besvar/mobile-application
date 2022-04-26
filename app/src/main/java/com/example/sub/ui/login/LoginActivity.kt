package com.example.sub.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sub.MainActivity

import com.example.sub.R
import com.example.sub.data.LoggedInUser
import com.example.sub.data.Result

// for debugging purposes
const val LOGIN_DISABLED = false
const val AUTOLOGIN_DISABLED = false

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var HTTPResponseCode: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginViewModel = ViewModelProvider(this,
            LoginViewModelFactory(this))[LoginViewModel::class.java]

        // logout if LoginActivity is created from MainActivity
        val logoutRequired : Boolean = intent.getBooleanExtra("logout", false)
        if (logoutRequired) {
            loginViewModel.logout()
        }

        // removes loggedInUser object from SharedPreferences so that no loggedInUser can be used for autologin. Only for debugging
        if (AUTOLOGIN_DISABLED) {
            Log.d("myDebug", "AUTOLOGIN_DISABLED")
            val sharedPref = this.getSharedPreferences("UserSharedPref", Context.MODE_PRIVATE)
            sharedPref!!.edit().clear().apply()
        }

        // skips the login fragment, only for debugging
        if (LOGIN_DISABLED) {
            startMainActivity(LoggedInUser(null,null))
        }

        // starts MainActivity if loggedInUser is saved and has a valid JWT token.
        if (loginViewModel.isLoggedIn()) {
            if (loginViewModel.getUser() != null) {
                startMainActivity(loginViewModel.getUser()!!)
            }
        }

        Log.d("myDebug", "loginViewModel.getUser(): " + loginViewModel.getUser())
    }

    /**
     * Starts mainActivity and finish the LoginActivity.
     */
    fun startMainActivity(loggedInUser : LoggedInUser) {
        let{
            val intent = Intent(it, MainActivity::class.java)
            intent.putExtra("loggedInUser", loggedInUser)
            it.startActivity(intent)
        }
        finish()
    }

    fun getLoginViewModel(): LoginViewModel {
        return loginViewModel
    }

    fun setHTTPResponseCode(r: String) {
        HTTPResponseCode = r
    }

}

