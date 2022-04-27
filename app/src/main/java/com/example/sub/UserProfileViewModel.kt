package com.example.sub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONTokener

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val users: MutableLiveData<List<User>> = MutableLiveData()
    private var userToken = ""
    private var errorMessage = ""
    private val context = getApplication<Application>().applicationContext

    /** Function to fetch the JWT userToken **/
    fun getUserToken(): String {
        return userToken
    }

    /** Function to set the JWT userToken for the ProfileFragmentViewModel **/
    fun setUserToken(token: String){
        userToken = token

    }

    suspend fun removeContact(phone: String) {
        val client = HttpClient(CIO){
            install(ContentNegotiation){
                json()
            }
        }
        var token: String = userToken
        token = token.drop(1)
        token = token.dropLast(1)
        val response: io.ktor.client.statement.HttpResponse = client.delete("http://144.24.171.133:8080/delete_contact") {
            contentType(ContentType.Application.Json)
            setBody(Contact(phone))
            headers {
                append(HttpHeaders.Accept, "*/*")
                append(HttpHeaders.UserAgent, "ktor client")
                append(HttpHeaders.Authorization, token)
            }
        }
        println(response.toString())
    }
}