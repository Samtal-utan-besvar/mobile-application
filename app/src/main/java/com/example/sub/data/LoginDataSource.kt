package com.example.sub.data

import android.util.Log
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONTokener
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
            val fakeUser = LoggedInUser("1","Jane Registration",null,null,null)
            Result.Success(fakeUser)
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    /**
     * Function that handles loggedInUser authentication with the database server.
     * <p>
     *     Returns a Result<LoggedInUser> object that specifies if a login HTTP Post request
     *     succeeded or received an error. On success, a loggedInUser object is created containing
     *     a user token received from the Post request.
     * <p>
     *     The function is using CoroutineScope, so that the HTTP Request is not running on the
     *     main thread. The function is, for that reason, suspended.
     * <p>
     *     result.component1(): Value of a successful result
     *     result.component2(): Value of a error
     */
    suspend fun login(email: String, password: String): Result<LoggedInUser> {
        return try {
            withContext(Dispatchers.IO) {
                val loginURL = url + "login"
                val user = LoginCredentials(email, password)
                val (_, _, result) = loginURL.httpPost()
                    .jsonBody(Gson().toJson(user).toString())
                    .responseString()
                if (result.component1() == null) {
                    Result.Error(IOException("Error logging in"))
                } else {
                    Log.d("myDebug", "Result from login: $result")
                    val token = result.component1()!!.removeQuotationMarks()
                    val loggedInUser = LoggedInUser(token,null,null,null, email)
                    Result.Success(loggedInUser)
                }
            }
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    /**
     * Revoke authentication of JWT token from database.
     * <p>
     *     Note: not used in this implementation of the code
     */
    fun logout() {
        // TODO: revoke authentication
    }

    /**
     * Update JWT token.
     * <p>
     *     JWT tokens from the database server expires within one week. If the user opens the app
     *     with a valid JWT token (less then 7 days after last use), the old token is updated.
     */
    suspend fun updateJWTToken(loggedInUser: LoggedInUser): Result<LoggedInUser> {
        val client = HttpClient(CIO)
        val token: String = loggedInUser.userToken.toString()
        val response: HttpResponse = client.request(url + "authenticate") {
            method = HttpMethod.Get
            headers {
                append(HttpHeaders.Accept, "*/*")
                append(HttpHeaders.UserAgent, "ktor client")
                append(HttpHeaders.Authorization, token)
            }
        }
        return if (response.status.toString() == "200 OK") {
            loggedInUser.userToken = response.bodyAsText().removeQuotationMarks()
            Result.Success(loggedInUser)
        } else {
            Result.Error(IOException(response.status.toString()))
        }
    }

    suspend fun getUserInformation(loggedInUser: LoggedInUser): Result<LoggedInUser> {
        val client = HttpClient(CIO)
        val token: String = loggedInUser.userToken.toString()
        val response: HttpResponse = client.request(url + "get_user") {
            method = HttpMethod.Get
            headers {
                append(HttpHeaders.Accept, "*/*")
                append(HttpHeaders.UserAgent, "ktor client")
                append(HttpHeaders.Authorization, token)
            }
        }
        return if (response.status.toString() == "200 OK") {
            val stringBody : String = response.body()
            Log.d("myDebug", "stringBody:   " + stringBody)
            val jsonArray = JSONTokener(stringBody).nextValue() as JSONArray
            loggedInUser.firstName = jsonArray.getJSONObject(0).getString("firstname")
            loggedInUser.lastName = jsonArray.getJSONObject(0).getString("lastname")
            loggedInUser.phoneNumber = jsonArray.getJSONObject(0).getString("phone_number")
            loggedInUser.email = jsonArray.getJSONObject(0).getString("email")
            Result.Success(loggedInUser)
        } else {
            Result.Error(IOException(response.status.toString()))
        }
    }

    /**
     * Removes in-String quotation marks "".
     * <p>
     *     Converting String from "YOUR_STRING" to YOUR_STRING.
     */
    private fun String.removeQuotationMarks(): String {
        if (startsWith("\"") && endsWith("\"")) {
            return drop(1).dropLast(1)
        }
        return this
    }
}


/**
 * Data class that contains login information used to make an HTTP Post request
 */
data class LoginCredentials(
    val email: String,
    val password: String
)

