package com.example.sub

class User(
        user_id: String, firstname: String, lastname: String, phone_number: String,
        email: String, password_hash: String) {
        var userId : String = user_id
        var firstName: String? = firstname
        var lastName: String? = lastname
        var number: String = phone_number
        var eMail : String? = email
        var passwordHash: String? = password_hash
}