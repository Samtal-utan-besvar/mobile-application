package com.example.sub

import kotlinx.serialization.Serializable


class User(
        firstname: String, lastname: String, phone_number: String) {
        //var userId : String = user_id
        var firstName: String? = firstname
        var lastName: String? = lastname
        var number: String = phone_number
        //var eMail : String? = email
        //var passwordHash: String? = password_hash
}