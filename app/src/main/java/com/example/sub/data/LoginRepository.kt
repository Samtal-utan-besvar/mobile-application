package com.example.sub.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource, context: Context?) {

    private val sharedPref = context!!.getSharedPreferences("UserSharedPref", Context.MODE_PRIVATE)

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    // returns true if a LoggedInUser object is saved in SharedPreferences
    val isLoggedIn: Boolean
        get() = sharedPref.getString("LOGGED_IN_USER", null) != null

    init {
        user = if (isLoggedIn) {
            runBlocking {
                readLoggedInUser()
            }
        } else {
            null
        }
    }

    /**
     * Revoke authentication from the database and removes the loggedInUser object from the
     * SharedPreferences.
     */
    fun logout() {
        user = null
        dataSource.logout()
        removeLoggedInUser()
        Log.d("myDebug", "(LOGOUT):\t" + user.toString())   // For debugging, should log null
    }

    /**
     * Calls login from the data source and saves the loggedInUser object if the result succeeded.
     */
    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        val loginResult = dataSource.login(username, password)
        Log.d("myDebug", "loginResult:   " + loginResult)
        if (loginResult is Result.Success) {
            val getUserResult = dataSource.getUserInformation(loginResult.data)
            if (getUserResult is Result.Success) {
                setLoggedInUser(getUserResult.data)
            }
        }
        Log.d("myDebug", "(LOGIN):\t" + user.toString())
        return loginResult
    }

    /**
     * Calls register from the data source and saves the loggedInUser object if the result
     * succeeded.
     */
    suspend fun register(username: String, password: String, cpassword: String, name: String,
                         surname: String, email: String): Result<LoggedInUser> {
        val result = dataSource.register(username, password, name, surname, email)
        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }
        return result
    }

    /**
     * Updates in-memory cache of the loggedInUser object and saves the object.
     */
    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        saveLoggedInUser(loggedInUser)
    }

    /**
     * Saves loggedInUser object to SharedPreferences.
     */
    private fun saveLoggedInUser(loggedInUser: LoggedInUser) {
        val gson = Gson()
        val json = gson.toJson(loggedInUser)
        sharedPref.edit().putString("LOGGED_IN_USER", json).apply()
    }

    /**
     * Return saved LoggedInUser object from SharedPreferences.
     */
    private suspend fun readLoggedInUser(): LoggedInUser? {
        val gson = Gson()
        val json = sharedPref.getString("LOGGED_IN_USER", null)
        return authenticate(gson.fromJson(json, LoggedInUser::class.java))
    }

    /**
     * Returns an authenticated LoggedInUser object on Result.Success. Return null on Result.Error.
     * <p>
     * The Authentication results in two possible outcomes: the JWT token becomes updated for
     * the saved LoggedInUser object, or the authentication becomes rejected; thus, the saved
     * JWT token of the object was too old.
     */
    private suspend fun authenticate(loggedInUser: LoggedInUser): LoggedInUser? {
        val result = dataSource.updateJWTToken(loggedInUser)
        if (result is Result.Success) {
            saveLoggedInUser(result.data)
            return result.data
        }
        return null
    }

    /**
     * Removes loggedInUser object from SharedPreferences.
     */
    private fun removeLoggedInUser() {
        sharedPref.edit().remove("LOGGED_IN_USER").apply()
    }
}