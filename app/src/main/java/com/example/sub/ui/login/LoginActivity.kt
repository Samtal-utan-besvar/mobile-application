package com.example.sub.ui.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.replace

import com.example.sub.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myDebug", " --- From LoginActivity --- ")
        setContentView(R.layout.activity_login)

//        supportFragmentManager.commit {
//            replace<LoginFragment>(R.id.container)
//            setReorderingAllowed(true)
//            addToBackStack("name") // name can be null
//        }
    }
}
