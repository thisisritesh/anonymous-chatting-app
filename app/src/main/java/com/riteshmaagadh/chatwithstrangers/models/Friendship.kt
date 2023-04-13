package com.riteshmaagadh.chatwithstrangers.models

import com.google.firebase.firestore.DocumentId

data class Friendship(
    @DocumentId var documentId: String,
    var room_id: String,
    var is_blocked: Boolean,
    var friend_username: String,
    var friend_profile_pic: String,
    var friend_document_id: String,
    var time_stamp_in_milliseconds: Long,
    var last_message: String
) {
    constructor() : this("", "",false, "", "", "", 0,"")
}