package com.example.sub.data

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userToken: String,
    val displayName: String
)