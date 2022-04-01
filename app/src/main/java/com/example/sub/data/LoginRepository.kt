package com.example.sub.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.sub.R
import com.example.sub.data.model.LoggedInUser
import com.google.gson.Gson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource, context: Context?) {

    private val sharedPref = context!!.getSharedPreferences("user_token", Context.MODE_PRIVATE)

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = sharedPref.getString("user_token", "NO_USER_LOGIN_SAVED") != "NO_USER_LOGIN_SAVED"

    init {
        generateKey()
        user = if (isLoggedIn) {
            readLoggedInUser()
        } else {
            null
        }
//        Log.d("myDebug", "user:$user")
    }


    fun logout() {
            user = null
            dataSource.logout()
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        val result = dataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
            saveCredentials("USER_TOKEN")   // TODO: change to actual USER_TOKEN given from successful login
        }
//        Log.d("myDebug", "readCredentials:" + readCredentials())
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
        sharedPref.edit().putString("loggedInUser", json).apply()
    }

    private fun readLoggedInUser(): LoggedInUser? {
        val gson = Gson()
        val json = sharedPref.getString("loggedInUser", "NO_LOGGED_IN_USER")
        return gson.fromJson(json, LoggedInUser::class.java)
    }

    private fun saveCredentials(data: String) {
        val encryptedData = encryptData(data)
        with(sharedPref.edit()) {
            putString("iv_bytes", Base64.encodeToString(encryptedData.first, Base64.DEFAULT))
            putString("user_token", Base64.encodeToString(encryptedData.second, Base64.DEFAULT))
            apply()
        }
    }

    private fun readCredentials(): String {
        val savedUserIV = sharedPref.getString("iv_bytes", "NO_USER_LOGIN_SAVED")
        val savedUserToken = sharedPref.getString("user_token", "NO_USER_LOGIN_SAVED")
        Log.d("myDebug", "dencrypted:" + decryptData(Base64.decode(savedUserIV, Base64.DEFAULT), Base64.decode(savedUserToken, Base64.DEFAULT)))
        return decryptData(Base64.decode(savedUserIV, Base64.DEFAULT), Base64.decode(savedUserToken, Base64.DEFAULT))
    }

    private fun getKey(): SecretKey {
        val keystore = KeyStore.getInstance("AndroidKeyStore")
        keystore.load(null)
        val secretKeyEntry = keystore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder("MyKeyAlias",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
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