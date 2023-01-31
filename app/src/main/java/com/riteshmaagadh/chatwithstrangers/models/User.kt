package com.riteshmaagadh.chatwithstrangers.models

import com.google.firebase.firestore.DocumentId

data class User(@DocumentId var documentId: String, var deviceId: String) {
    constructor() : this("","")
}
