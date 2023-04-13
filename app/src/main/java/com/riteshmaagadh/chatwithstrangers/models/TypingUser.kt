package com.riteshmaagadh.chatwithstrangers.models

import com.google.firebase.firestore.DocumentId

data class TypingUser(@DocumentId var documentID: String, var username: String) {
    constructor() : this("","")
}