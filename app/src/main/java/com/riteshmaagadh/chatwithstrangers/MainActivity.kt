package com.riteshmaagadh.chatwithstrangers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.util.Util
import com.google.firebase.firestore.CollectionReference
import com.riteshmaagadh.chatwithstrangers.callbacks.AlertDialogCallbacks
import com.riteshmaagadh.chatwithstrangers.databinding.ActivityMainBinding
import com.riteshmaagadh.chatwithstrangers.models.User
import org.json.JSONException
import org.json.JSONObject


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
    private val NONE = "none"

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
        Utils.makeStatusBarBlack(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = Pref(this)

        if (pref.getPrefBoolean(Constants.IS_APP_LOCK_ENABLED)) {
            Utils.showVerificationDialog(this, object : AlertDialogCallbacks {
                override fun onPositiveButtonClicked(string: String) {
                    if (pref.getPrefString(Constants.APP_LOCK_PASSWORD) != string) {
                        Utils.showInfoDialog(this@MainActivity, object : AlertDialogCallbacks {
                            override fun onPositiveButtonClicked(string: String) {
                                finish()
                            }

                            override fun onNegativeButtonClicked() {

                            }
                        }, "Wrong password!")
                    }
                }

                override fun onNegativeButtonClicked() {
                    finish()
                }
            }, "Enter PIN to verify!")
        }

        switchScreens(DiscoverFragment.newInstance())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.discover_menu -> {
                    binding.headingTv.text = getString(R.string.discover)
                    switchScreens(DiscoverFragment.newInstance())
                }
                R.id.chat_menu -> {
                    binding.headingTv.text = getString(R.string.chat)
                    switchScreens(ChatsFragment.newInstance())
                }
                R.id.profile_menu -> {
                    binding.headingTv.text = getString(R.string.account)
                    switchScreens(AccountFragment.newInstance())
                }
                else -> {false}
            }
        }


        androidId = Secure.getString(
            this.contentResolver,
            Secure.ANDROID_ID
        )


        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()

    }

    private fun switchScreens(fragment: Fragment) : Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
        return true
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