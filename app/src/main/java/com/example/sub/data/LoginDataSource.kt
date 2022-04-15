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

    private val url = "http://144.24.171.133:8080/" // home url to database server
    private val urlLocal = "http://10.0.2.2:8080/"  // local url to database. Emulator requires '10.0.2.2' instead of 'localhost'


    suspend fun register(username: String, password: String): Result<LoggedInUser> {
        return try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser("1", "Jane Registration")
            Result.Success(fakeUser)
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    /**
     * Function that handles loggedInUser authentication with the database server.
     * <p>
     * Returns a Result<LoggedInUser> object that specifies if a login HTTP Post request succeeded
     * or received an error. On success, a loggedInUser object is created containing a user token
     * received from the Post request.
     * <p>
     * The function is using CoroutineScope, so that the HTTP Request is not running on the main
     * thread. The function is, for that reason, suspended.
     */
    suspend fun login(email: String, password: String): Result<LoggedInUser> {
        return try {
            withContext(Dispatchers.IO) {
                val loginURL = url + "login"
                val user = LoginCredentials(email, password)
                val (_, _, result) = loginURL.httpPost()
                    .jsonBody(Gson().toJson(user).toString())
                    .responseString()

                // result.component1(): Value of a successful result
                // result.component2(): Value of a error

                if (result.component1() == null) {
                    Result.Error(IOException("Error logging in"))
                } else {
                    Log.d("myDebug", "Result: $result")
                    val loggedInUser = LoggedInUser(result.component1(), null)
                    Result.Success(loggedInUser)
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


/**
 * Data class that contains login information used to make an HTTP Post request
 */
data class LoginCredentials(
    val email: String,
    val password: String
)

