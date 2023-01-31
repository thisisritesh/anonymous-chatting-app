package com.riteshmaagadh.chatwithstrangers.models

import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId var documentId: String,
    var message: String,
    var timestamp: String,
    var senderDeviceId: String,
    var deviceDetail: String
) {
    constructor() : this("","","","","")
}