package com.example.sub.data

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.sub.data.model.LoggedInUser
import com.google.gson.Gson
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource, context: Context?) {

    private val sharedPref = context!!.getSharedPreferences("UserSharedPref", Context.MODE_PRIVATE)

    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = sharedPref.getString("USER_TOKEN", null) != null

    init {
        user = if (isLoggedIn) {
            readLoggedInUser()
        } else {
            null
        }
    }

    fun logout() {
        Log.d("myDebug", "(LOG-OUT) Credentials in sharedPref   : " + readCredentials())
        Log.d("myDebug", "(LOG-OUT) LoggedInUser in memory      : $user")
        Log.d("myDebug", "(LOG-OUT) LoggedInUser in sharedPref  : " + readLoggedInUser())
        Log.d("myDebug", "_________________________________")
        user = null
        dataSource.logout()
        removeLoggedInUser()
        removeCredentials()
        Log.d("myDebug", "(LOG-OUT) Credentials in sharedPref   : " + readCredentials())
        Log.d("myDebug", "(LOG-OUT) LoggedInUser in memory      : $user")
        Log.d("myDebug", "(LOG-OUT) LoggedInUser in sharedPref  : " + readLoggedInUser())
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        val result = dataSource.login(username, password)
        if (result is Result.Success) {
            setLoggedInUser(result.data)
            saveCredentials("USER_TOKEN")   // TODO: change to actual USER_TOKEN given from successful login
        }
        Log.d("myDebug", "_________________________________")
        Log.d("myDebug", "(LOG-IN) Credentials in sharedPref    : " + readCredentials())
        Log.d("myDebug", "(LOG-IN) User in memory               : $user")
        Log.d("myDebug", "(LOG-IN) LoggedInUser in sharedPref   : " + readLoggedInUser())
        return result
    }

    fun register(username: String, password: String): Result<LoggedInUser> {
        val result = dataSource.register(username, password)
        if (result is Result.Success) {
            setLoggedInUser(result.data)
            saveCredentials("USER_TOKEN")   // TODO: change to actual USER_TOKEN given from successful login
        }
        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        saveLoggedInUser(loggedInUser)
    }

    private fun saveLoggedInUser(loggedInUser: LoggedInUser) {
        val gson = Gson()
        val json = gson.toJson(loggedInUser)
        sharedPref.edit().putString("LOGGED_IN_USER", json).apply()
    }

    private fun readLoggedInUser(): LoggedInUser? {
        val gson = Gson()
        val json = sharedPref.getString("LOGGED_IN_USER", null) ?: return null
        return gson.fromJson(json, LoggedInUser::class.java)
    }

    private fun removeLoggedInUser() {
        sharedPref.edit().remove("LOGGED_IN_USER").apply()
    }

    private fun saveCredentials(data: String) {
        val encryptedData = encryptData(data)
        with(sharedPref.edit()) {
            putString("IV_BYTES", Base64.encodeToString(encryptedData.first, Base64.DEFAULT))
            putString("USER_TOKEN", Base64.encodeToString(encryptedData.second, Base64.DEFAULT))
            apply()
        }
    }

    fun readCredentials(): String? {
        val savedUserIV = sharedPref.getString("IV_BYTES", null) ?: return null
        val savedUserToken = sharedPref.getString("USER_TOKEN", null) ?: return null
        return decryptData(
            Base64.decode(savedUserIV, Base64.DEFAULT),
            Base64.decode(savedUserToken, Base64.DEFAULT)
        )
    }

    private fun removeCredentials() {
        sharedPref.edit().remove("IV_BYTES").apply()
        sharedPref.edit().remove("USER_TOKEN").apply()
        sharedPref.edit().remove("KEY").apply()
        deleteKey()
    }

    private fun generateSecretKey(): SecretKey? {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator?.init(128, SecureRandom())
        return keyGenerator?.generateKey()
    }

    private fun saveKey(secretKey: SecretKey): String {
        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
        sharedPref.edit().putString("KEY", encodedKey).apply()
        return encodedKey
    }

    private fun getKey(): SecretKey {
        val key = sharedPref.getString("KEY", null)
        if (key == null) {
            val secretKey = generateSecretKey()
            saveKey(secretKey!!)
            return secretKey
        }
        val decodedKey = Base64.decode(key, Base64.NO_WRAP)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    private fun deleteKey() {
        val keystore = KeyStore.getInstance("AndroidKeyStore")
        keystore.load(null)
        keystore.deleteEntry("MyKeyAlias")
    }

    private fun encryptData(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        var temp = data
        while (temp.toByteArray().size % 16 != 0)
            temp += "\u0020"
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val ivBytes = cipher.iv
        val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))
        return Pair(ivBytes, encryptedBytes)
    }

    private fun decryptData(ivBytes: ByteArray, data: ByteArray): String{
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val spec = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
    }
}


