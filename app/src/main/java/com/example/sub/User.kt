package com.example.sub

import kotlinx.serialization.Serializable

/** User used primarily in the contactlist **/
class User(
        firstname: String, lastname: String, phone_number: String) {
        var firstName: String? = firstname
        var lastName: String? = lastname
        var number: String = phone_number
}