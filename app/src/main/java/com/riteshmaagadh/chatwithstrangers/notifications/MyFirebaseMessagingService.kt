package com.riteshmaagadh.chatwithstrangers.notifications

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.riteshmaagadh.chatwithstrangers.ChatActivity

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @SuppressLint("HardwareIds")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        val intent = Intent()
        intent.action = "OPEN_NEW_ACTIVITY"
        intent.putExtra("device_id", message.data["device_id"])
        intent.putExtra("current_time", message.data["current_time"])
        sendBroadcast(intent)

    }


}