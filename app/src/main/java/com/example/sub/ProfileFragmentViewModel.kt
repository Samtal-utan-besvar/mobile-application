package com.example.sub

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

internal class ProfileFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val users: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>().also {
            loadUsers()
        }
    }
    private var testuser = ""
    private val context = getApplication<Application>().applicationContext

    /**fun getUsers(): LiveData<List<User>> {
        val returnstring = loadUsers()
        return returnstring
    }**/

    fun getUsers(): String {
        val returnstring = loadUsers()
        return returnstring
    }

    private fun loadUsers(): String {
        println("akjdhawhkdawkhdakhwdjkawkadkjwbnkdakbjdbjk")
        val url = "http://144.24.171.133:8080/get_contacts"
        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, url,
             Response.Listener<String> { response ->
                 testuser = response.substring(0,500)
        },
        Response.ErrorListener { testuser = "Theres an error"})
        queue.add(stringRequest)
        
        println(testuser)
        return testuser
}
}