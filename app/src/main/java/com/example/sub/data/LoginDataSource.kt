package com.example.sub.data

import android.util.Log
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private val url = "http://144.24.171.133:8080/"
    private val urlLocal = "http://10.0.2.2:8080/"

    fun register(username: String, password: String): Result<LoggedInUser> {
        return try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser("1", "Jane Registration")
            Result.Success(fakeUser)
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    suspend fun login(email: String, password: String): Result<LoggedInUser> {
        return try {
            withContext(Dispatchers.IO) {
                val loginURL = urlLocal + "login"
                val user = LoginCredentials(email, password)
                val (_, _, result) = loginURL.httpPost()
                    .jsonBody(Gson().toJson(user).toString())
                    .responseString()

                // TODO: use result.onError and result.success instead?
                if (result.component1() == null) {
                    Result.Error(IOException("Error logging in"))
                } else {
                    Log.d("myDebug", "Result: $result")
                    val fakeUser = LoggedInUser(result.component1(), null)
                    Result.Success(fakeUser)
                }
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }


    fun logout() {
        // TODO: revoke authentication
    }
}

data class LoginCredentials(
    val email: String,
    val password: String
)

