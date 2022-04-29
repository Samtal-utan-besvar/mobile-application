package com.example.sub.data
import java.io.Serializable

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser (
    var userToken: String?,
    var phoneNumber: String?,
    var firstName: String?,
    var lastName: String?,
    var email: String?
) : Serializable