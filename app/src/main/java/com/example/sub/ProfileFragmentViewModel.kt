package com.example.sub

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Accept
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONTokener
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*


internal class ProfileFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val users: MutableLiveData<List<User>> = MutableLiveData()
    private var userToken = ""
    private var errorMessage = ""
    private val context = getApplication<Application>().applicationContext
    private val url = "http://144.24.171.133:8080/" // home url to database server
    private val urlLocal = "http://10.0.2.2:8080/"  // local url to database. Emulator requires '10.0.2.2' instead of 'localhost'


    /** Fetches all contacts of a user to put in the contactlist **/
    fun getUsers(): LiveData<List<User>> {
        runBlocking {loadUsers()}
        return users
    }

    /** Function to fetch the JWT userToken **/
    fun getUserToken(): String {
        return userToken
    }

    /** Function to set the JWT userToken for the ProfileFragmentViewModel **/
    fun setUserToken(token: String){
        userToken = token

    }

    /** Function to fetch all contacts of a user and put them in a list to be used
     * in the contactlist
     */
    private suspend fun loadUsers() {
        var allUsers: MutableList<User> = ArrayList()
        var token : String = userToken
        val client = HttpClient(CIO)
        val response: HttpResponse = client.request(url + "get_contacts") {
            method = HttpMethod.Get
            headers{
                append(Accept, "*/*")
                append(HttpHeaders.UserAgent, "ktor client")
                append(HttpHeaders.Authorization, token)
            }
        }
        val stringbody : String = response.body()
        val jsonArray = JSONTokener(stringbody).nextValue() as JSONArray
        for (i in 0 until jsonArray.length()) {
            val firstName = jsonArray.getJSONObject(i).getString("firstname")
            val lastName = jsonArray.getJSONObject(i).getString("lastname")
            val phoneNumber = jsonArray.getJSONObject(i).getString("phone_number")
            val user = User(firstName, lastName, phoneNumber)
            allUsers.add(user)
            users.postValue(allUsers)
        }
    }

    /** Adds a contact to the list and then fetches the updated Contactlist **/
    suspend fun addContact(phone: String) {
        val client = HttpClient(CIO){
            install(ContentNegotiation){
                json()
            }
        }
        var token: String = userToken
        val response: HttpResponse = client.post(url + "add_contact") {
            contentType(ContentType.Application.Json)
            setBody(Contact(phone))
            headers {
                append(Accept, "*/*")
                append(HttpHeaders.UserAgent, "ktor client")
                append(HttpHeaders.Authorization, token)
            }
        }
        println(response.toString())
        getUsers()
    }
}