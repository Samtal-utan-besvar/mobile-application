package com.example.sub

import kotlinx.serialization.Serializable

/** A serializable contact class used when adding a contact **/
@Serializable
data class Contact (
    val contact_phonenumber: String
    ) {
}