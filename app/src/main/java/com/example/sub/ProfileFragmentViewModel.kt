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
import com.google.gson.Gson
import com.example.sub.User
import com.example.sub.Users
import org.json.JSONArray
import org.json.JSONTokener

internal class ProfileFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val users: MutableLiveData<List<User>> = MutableLiveData()
    private var testuser = ""
    private val context = getApplication<Application>().applicationContext

    /**fun getUsers(): LiveData<List<User>> {
        val returnstring = loadUsers()
        return returnstring
    }**/

    fun getUsers(): LiveData<List<User>> {
        val returnstring = loadUsers()
        return users
    }

    private fun loadUsers() {
        var allUsers: MutableList<User> = ArrayList()
        val url = "http://144.24.171.133:8080/get_users"
        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val jsonArray = JSONTokener(response).nextValue() as JSONArray
                for (i in 0 until jsonArray.length()){
                    val userID = jsonArray.getJSONObject(i).getString("user_id")
                    val firstName = jsonArray.getJSONObject(i).getString("firstname")
                    val lastName = jsonArray.getJSONObject(i).getString("lastname")
                    val phoneNumber = jsonArray.getJSONObject(i).getString("phone_number")
                    val eMail = jsonArray.getJSONObject(i).getString("email")
                    val passwordHash = jsonArray.getJSONObject(i).getString("password_hash")
                    val user = User(userID, firstName, lastName, phoneNumber, eMail, passwordHash)
                    allUsers.add(user)
                    users.postValue(allUsers)
                }
       },
            { testuser = "Theres an error"})
        queue.add(stringRequest)
        print(allUsers.size)
}
}