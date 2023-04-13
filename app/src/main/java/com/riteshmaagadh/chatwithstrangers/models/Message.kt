package com.riteshmaagadh.chatwithstrangers.models

import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId var documentId: String,
    var index: Int,
    var media_url: String,
    var message: String,
    var sender_device_detail: String,
    var sent_by: String,
    var timestamp: String,
    var time_stamp_in_milliseconds: Long,
    var date: String
) {
    constructor() : this("",0,"","","","","", 0,"")
}