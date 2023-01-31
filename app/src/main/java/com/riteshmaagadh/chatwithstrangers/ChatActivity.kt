package com.riteshmaagadh.chatwithstrangers

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.util.Util
import com.google.firebase.firestore.*
import com.riteshmaagadh.chatwithstrangers.adapters.MessagesAdapter
import com.riteshmaagadh.chatwithstrangers.callbacks.AlertDialogCallbacks
import com.riteshmaagadh.chatwithstrangers.databinding.ActivityChatBinding
import com.riteshmaagadh.chatwithstrangers.models.Message
import com.riteshmaagadh.chatwithstrangers.models.TypingUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var collectionRefId: String
    private lateinit var currentTime: String
    private var messages: ArrayList<Message> = arrayListOf()
    private lateinit var deviceId: String
    private lateinit var otherDeviceId: String
    private lateinit var adapter: MessagesAdapter
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    companion object {
        private const val TAG = "ChatActivity"
    }

    @SuppressLint("HardwareIds", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.dark_color)
            window.navigationBarColor = getColor(R.color.dark_color)
        }
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceId = Settings.Secure.getString(
            this.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        binding.messageEditText.addTextChangedListener(textWatcher)

        collectionRefId = intent.extras?.getString("device_id")!!
        currentTime = intent.extras?.getString("current_time")!!

        mLinearLayoutManager = LinearLayoutManager(this)
        binding.chatsRecyclerView.layoutManager = mLinearLayoutManager
        adapter = MessagesAdapter(messages, deviceId)
        binding.chatsRecyclerView.adapter = adapter

        lifecycleScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance()
                .collection(collectionRefId + "_" + currentTime)
                .addSnapshotListener { value, error ->
                    if (error == null) {
                        try {
                            val newMessage =
                                value?.documentChanges?.get(0)?.document?.toObject(Message::class.java)
                            if (newMessage?.senderDeviceId!! != deviceId) {
                                otherDeviceId = newMessage.senderDeviceId
                            }
                            messages.add(newMessage!!)
                            adapter.notifyDataSetChanged()
                            mLinearLayoutManager.scrollToPosition(messages.size - 1)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        }

        binding.sendBtn.setOnClickListener {
            sendMessage()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance()
                .collection("typing_users")
                .addSnapshotListener { value, error ->
                    if (error == null) {
                        val users = value?.toObjects(TypingUser::class.java)
                        if (users != null) {
                            if (users.size == 0) {
                                binding.textView.text = ""
                            } else {
                                for (user in users) {
                                    if (this@ChatActivity::otherDeviceId.isInitialized) {
                                        if (user.deviceId == otherDeviceId) {
                                            binding.textView.text = "Stranger is typing..."
                                            break
                                        } else {
                                            binding.textView.text = ""
                                        }
                                    }
                                }
                            }
                        } else {
                            binding.textView.text = ""
                        }
                    }
                }

        }

        binding.skipBtn.setOnClickListener {
//            Utils.showSkipUserDialog(this, object : AlertDialogCallbacks {
//                override fun onPositiveButtonClicked() {
//
//                }
//
//                override fun onNegativeButtonClicked() {
//
//                }
//            }, getString(R.string.do_you_really_want_to_skip_start_chat_with_another_user))
            Utils.showSkipUserDialog(this, object : AlertDialogCallbacks {
                override fun onPositiveButtonClicked() {
                    finish()
                }

                override fun onNegativeButtonClicked() {}
            }, "Do you really want to exit?")
        }


    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.sendBtn.isEnabled = !p0.isNullOrEmpty()
            if (p0.isNullOrEmpty()) {
                sendNotTypingStatus()
            } else if (p0.toString().length == 1) {
                sendTypingStatus()
            }
        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0.isNullOrEmpty()) {
                sendNotTypingStatus()
            }
        }
    }

    private fun sendTypingStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            val typingUser = TypingUser("", deviceId)
            FirebaseFirestore.getInstance()
                .collection("typing_users")
                .add(typingUser)
        }
    }

    private fun sendNotTypingStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance()
                .collection("typing_users")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val users = it.toObjects(TypingUser::class.java)
                        try {
                            if (users != null) {
                                FirebaseFirestore.getInstance()
                                    .collection("typing_users")
                                    .document(users[0].documentId)
                                    .delete()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        }
    }

    override fun finish() {
        sendNotTypingStatus()
        super.finish()
    }

    override fun onBackPressed() {
        Utils.showSkipUserDialog(this, object : AlertDialogCallbacks {
            override fun onPositiveButtonClicked() {
                finish()
            }

            override fun onNegativeButtonClicked() {}
        }, "Do you really want to exit?")
    }

    @SuppressLint("HardwareIds")
    private fun sendMessage() {
        val msg = binding.messageEditText.text.toString().trim()
        val message = Message(
            "",
            msg,
            getDateTime(),
            deviceId,
            Build.MODEL
        )

        lifecycleScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance()
                .collection(collectionRefId + "_" + currentTime)
                .add(message)
                .addOnSuccessListener {
                    binding.sendBtn.isEnabled = false
                }
                .addOnFailureListener {
                    binding.messageEditText.setText(msg)
                    Toast.makeText(this@ChatActivity, "Failed! Try Again.", Toast.LENGTH_SHORT).show()
                }
        }

        binding.messageEditText.setText("")

    }

    private fun getDateTime(): String = DateFormat.getDateTimeInstance().format(Date())

}