package com.example.sub.ui.login

import android.telephony.PhoneNumberUtils
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sub.R
import com.example.sub.data.LoginRepository
import com.example.sub.data.Result
import com.example.sub.data.LoggedInUser

class LoginViewModel(val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    suspend fun login(username: String, password: String) {
        val result = loginRepository.login(username, password)
        if (result is Result.Success) {
            _loginResult.postValue(LoginResult(success = LoggedInUserView(displayName = result.data.displayName)))
        } else {
            _loginResult.postValue(LoginResult(error = R.string.login_failed))
        }
    }

    suspend fun register(username: String, password: String) {
        val result = loginRepository.register(username, password)
        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.registration_failed)
        }
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(phoneNumberError = R.string.invalid_mail)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isEmailValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 5
    }

    fun isLoggedIn(): Boolean {
        return loginRepository.isLoggedIn
    }

    fun getUser(): LoggedInUser? {
        return loginRepository.user
    }
}