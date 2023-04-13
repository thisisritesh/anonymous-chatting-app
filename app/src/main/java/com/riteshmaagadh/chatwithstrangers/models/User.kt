package com.riteshmaagadh.chatwithstrangers.models

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId var documentId: String,
    var avatar_url: String,
    var email: String,
    var display_name: String,
    var bio: String,
    var uid: String,
    var password: String,
    var last_seen: String,
    var device_detail: String,
    var joined_at: String
) {
    constructor() : this("", "", "", "", "","", "","", "","")

}
