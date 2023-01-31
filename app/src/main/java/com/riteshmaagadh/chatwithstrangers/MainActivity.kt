package com.riteshmaagadh.chatwithstrangers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.riteshmaagadh.chatwithstrangers.callbacks.AlertDialogCallbacks
import com.riteshmaagadh.chatwithstrangers.databinding.ActivityMainBinding
import com.riteshmaagadh.chatwithstrangers.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAPpSR25s:APA91bH-qZ8roIvkCXKsl133sJlqmYaqfFKj1-PE4RL-MPCe8AYoYDb5_YE4kbAifVUdU13HmUIMl5Y2Kra1oeAVnO392HOLN4_Lr5wnd40WloZt-POUcUrepbuv73IZyzXvbkmUwGqj"
    private val contentType = "application/json"
    private lateinit var androidId: String
    private lateinit var user: User
    private var documentId = ""
    private var broadcastReceiver: BroadcastReceiver? = null
    private lateinit var intentFilter: IntentFilter
    private lateinit var collectionRef: CollectionReference
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
    private lateinit var dialog: AlertDialog

    companion object {
        private const val TAG = "MainActivity"
    }

    @SuppressLint("HardwareIds", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.dark_color)
            window.navigationBarColor = getColor(R.color.dark_color)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        collectionRef = FirebaseFirestore.getInstance()
            .collection("waiting_list")


        collectionRef
            .addSnapshotListener { value, error ->
                if (error == null) {
                    binding.onlineUserCountTv.text =
                        "${value?.documents?.size!!} users are available to talk!"
                }
            }

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                try {
                    collectionRef
                        .document(Utils.getUserDocumentId(this@MainActivity))
                        .delete()
                        .addOnSuccessListener {
                            dialog.dismiss()
                            val i = Intent(this@MainActivity, ChatActivity::class.java)
                            val deviceId = p1?.extras?.getString("device_id")!!
                            val currentTime = p1.extras?.getString("current_time")!!
                            i.putExtra("device_id", deviceId)
                            i.putExtra("current_time", currentTime)
                            startActivity(i)
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "onCreate: ", it)
                        }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        intentFilter = IntentFilter("OPEN_NEW_ACTIVITY")


        androidId = Secure.getString(
            this.contentResolver,
            Secure.ANDROID_ID
        )


        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()


    }


    override fun onStart() {
        super.onStart()

        binding.startAChatBtn.setOnClickListener {

            Utils.showTermsDialog(this, object : AlertDialogCallbacks {
                override fun onPositiveButtonClicked() {
                    dialog.show()

                    user = User(documentId, androidId)

                    lifecycleScope.launch(Dispatchers.IO) {
                        collectionRef
                            .add(user)
                            .addOnSuccessListener {
                                Utils.setUserDocumentId(it.id, this@MainActivity)
                                documentId = it.id
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "onCreate: ", it)
                                Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        Log.d(TAG, "androidId: $androidId")
                        lifecycleScope.launch(Dispatchers.IO) {
                            collectionRef
                                .whereNotEqualTo("deviceId", androidId)
                                .get()
                                .addOnSuccessListener {
                                    val userList = it.toObjects(User::class.java)
                                    if (userList.isNotEmpty()) {
                                        FirebaseMessaging.getInstance()
                                            .subscribeToTopic("/topics/${userList[0].deviceId}")
                                            .addOnSuccessListener {
                                                notifyUser("/topics/${userList[0].deviceId}")
                                            }
                                    }
                                }
                        }
                    }

                }

                override fun onNegativeButtonClicked() {

                }
            })

        }


    }


    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, intentFilter)
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$androidId")
        Log.e(TAG, "androidId: $androidId")
    }


    override fun onPause() {
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
        if (documentId.isNotEmpty()) {
            collectionRef.document(documentId)
                .delete()
        }
        dialog.dismiss()
        super.onPause()
    }

    private fun notifyUser(topic: String) {
        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            val currentTime = System.currentTimeMillis().toString()
            notifcationBody.put("device_id", androidId)
            notifcationBody.put("current_time", currentTime)
            notification.put("to", topic)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        sendNotification(notification)
    }

    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, FCM_API, notification,
            Response.Listener { response ->
                Log.i("dfghsjdffff", "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(this@MainActivity, "Request error", Toast.LENGTH_LONG).show()
                Log.i("dfghsjdffff", "onErrorResponse: Didn't work", it)
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

}