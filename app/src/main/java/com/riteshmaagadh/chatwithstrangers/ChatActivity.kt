package com.riteshmaagadh.chatwithstrangers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.riteshmaagadh.chatwithstrangers.TimeUtils.toTimeAgo
import com.riteshmaagadh.chatwithstrangers.adapters.MessagesAdapter
import com.riteshmaagadh.chatwithstrangers.callbacks.AlertDialogCallbacks
import com.riteshmaagadh.chatwithstrangers.databinding.ActivityChatBinding
import com.riteshmaagadh.chatwithstrangers.models.Friendship
import com.riteshmaagadh.chatwithstrangers.models.Message
import com.riteshmaagadh.chatwithstrangers.models.TypingUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private var messages: ArrayList<Message> = arrayListOf()
    private lateinit var adapter: MessagesAdapter
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var roomId: String
    private lateinit var myProfilePic: String
    private lateinit var myUsername: String

    private var isFirstTime = true
    private var currentIndex = 0
    private var lastMessage: Message? = null
    private lateinit var otherUsername: String
    private lateinit var otherUserDocumentId: String
    private lateinit var otherUserProfilePic: String
    private lateinit var myDocumentId: String

    @SuppressLint("HardwareIds", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.makeStatusBarBlack(this)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.messageEditText.addTextChangedListener(textWatcher)

        otherUsername = intent.extras?.getString(Constants.OTHER_USERNAME)!!
        otherUserDocumentId = intent.extras?.getString(Constants.OTHER_USER_DOCUMENT_ID)!!
        otherUserProfilePic = intent.extras?.getString(Constants.OTHER_USER_PROFILE_PIC)!!
        val isFromChat = intent.extras?.getBoolean(Constants.IS_FROM_CHATS)!!

        val pref = Pref(this)
        myUsername = pref.getPrefString(Constants.USERNAME)
        myDocumentId = pref.getPrefString(Constants.MY_DOCUMENT_ID)
        myProfilePic = pref.getPrefString(Constants.MY_PROFILE_PIC)

        binding.textView.text = otherUsername
        Glide.with(this)
            .load(otherUserProfilePic)
            .into(binding.avatarImageView)

        mLinearLayoutManager = LinearLayoutManager(this)
        binding.chatsRecyclerView.layoutManager = mLinearLayoutManager
        adapter = MessagesAdapter(messages, myUsername, this)
        binding.chatsRecyclerView.adapter = adapter

        binding.sendBtn.setOnClickListener {
            sendMessage()
        }

        binding.skipBtn.setOnClickListener {
            Utils.showDialog(this, object : AlertDialogCallbacks {
                override fun onPositiveButtonClicked(string: String) {
                    finish()
                }

                override fun onNegativeButtonClicked() {}
            }, "Do you really want to exit?")
        }

        if (isFromChat) {
            roomId = intent.extras?.getString(Constants.ROOM_ID)!!
        } else {
            roomId = myUsername + "_" + otherUsername
            makeFriendship(myUsername, otherUsername, otherUserProfilePic)
        }

        FirebaseFirestore.getInstance()
            .collection(roomId)
            .addSnapshotListener { value, error ->
                if (error == null) {
                    try {
                        if (isFirstTime) {
                            val existingMessages = value?.toObjects(Message::class.java)!!
                            val sortedChats = sortChats(existingMessages as ArrayList<Message>)
                            lastMessage = sortedChats[sortedChats.size - 1]
                            currentIndex = lastMessage?.index!!
                            messages.addAll(sortedChats)
                        } else {
                            val newMessage =
                                value?.documentChanges?.get(0)?.document?.toObject(Message::class.java)
                            currentIndex = newMessage?.index!!
                            messages.add(newMessage)
                        }
                        adapter.notifyDataSetChanged()
                        mLinearLayoutManager.scrollToPosition(messages.size - 1)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }


        FirebaseFirestore.getInstance()
            .collection(roomId + "_typing")
            .addSnapshotListener { value, error ->
                if (error == null) {
                    val typingUsers = value?.toObjects(TypingUser::class.java)!!
                    if (!typingUsers.isNullOrEmpty()) {
                        toggleTypingView(typingUsers)
                    } else {
                        binding.typingLottieView.pauseAnimation()
                        binding.typingLottieView.visibility = View.GONE
                    }
                }
            }

        binding.imagePickerBtn.setOnClickListener {
            chooseImage()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, dataIntent)
        var path: Uri? = null
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            if (dataIntent == null || dataIntent.data == null) {
                return
            }
            path = dataIntent.data
            try {
                if (path != null) {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(contentResolver, path)
                    val boas = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20,boas)
                    val ref = FirebaseStorage.getInstance()
                        .reference.child(myUsername + "_" + System.currentTimeMillis() + ".jpg")
                    ref.putBytes(boas.toByteArray())
                        .addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener {
                                val deviceDetail = Utils.getDeviceDetail(this)
                                val message = Message(
                                    "",
                                    currentIndex + 1,
                                    it.toString(),
                                    "",
                                    deviceDetail,
                                    myUsername,
                                    Utils.getMessageTime(),
                                    System.currentTimeMillis(),
                                    Utils.getDate()
                                )

                                lifecycleScope.launch(Dispatchers.IO) {
                                    FirebaseFirestore.getInstance()
                                        .collection(roomId)
                                        .add(message)
                                        .addOnSuccessListener {
                                            binding.sendBtn.isEnabled = false
                                        }
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this,"Failed to upload image! Try Again.",Toast.LENGTH_SHORT).show()
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1001)
    }


    private fun toggleTypingView(typingUsers: List<TypingUser>) {
        for (typingUser in typingUsers) {
            if (typingUser.username == otherUsername) {
                binding.typingLottieView.visibility = View.VISIBLE
                binding.typingLottieView.playAnimation()
                return
            }
        }
        binding.typingLottieView.pauseAnimation()
        binding.typingLottieView.visibility = View.GONE
    }

    private fun sortChats(messages: ArrayList<Message>): List<Message> {
        return ArrayList(messages)
            .sortedWith(compareByDescending<Message> { it.index }).reversed()
    }

    private fun makeFriendship(
        myUsername: String,
        otherUsername: String,
        otherUserProfilePic: String
    ) {
        FirebaseFirestore.getInstance()
            .collection(Constants.USERS)
            .document(myDocumentId)
            .collection(Constants.FRIENDSHIPS)
            .add(Friendship("", roomId, otherUsername, otherUserProfilePic, otherUserDocumentId, System.currentTimeMillis(),""))
            .addOnSuccessListener {
                FirebaseFirestore.getInstance()
                    .collection(Constants.USERS)
                    .document(otherUserDocumentId)
                    .collection(Constants.FRIENDSHIPS)
                    .add(Friendship("", roomId, myUsername, myProfilePic, myDocumentId, System.currentTimeMillis(),""))
            }
    }

    private fun addLastMessageToFriendShip(
        otherUsername: String
    ) {
        val updateMap: Map<String, Any> = mutableMapOf(
            Pair("time_stamp_in_milliseconds", lastMessage?.time_stamp_in_milliseconds!!),
            Pair("last_message", lastMessage?.message!!)
        )
        FirebaseFirestore.getInstance()
            .collection(Constants.USERS)
            .document(myDocumentId)
            .collection(Constants.FRIENDSHIPS)
            .whereEqualTo("friend_username", otherUsername)
            .get()
            .addOnSuccessListener {
                val friendships = it.toObjects(Friendship::class.java)
                if (!friendships.isNullOrEmpty()) {
                    val friendship = friendships[0]
                    FirebaseFirestore.getInstance()
                        .collection(Constants.USERS)
                        .document(myDocumentId)
                        .collection(Constants.FRIENDSHIPS)
                        .document(friendship.documentId)
                        .update(updateMap)
                        .addOnSuccessListener {
                            FirebaseFirestore.getInstance()
                            .collection(Constants.USERS)
                            .document(otherUserDocumentId)
                            .collection(Constants.FRIENDSHIPS)
                            .whereEqualTo("friend_username", myUsername)
                            .get()
                            .addOnSuccessListener {
                                val friendships1 = it.toObjects(Friendship::class.java)
                                if (!friendships1.isNullOrEmpty()) {
                                    val friendship1 = friendships1[0]
                                    FirebaseFirestore.getInstance()
                                        .collection(Constants.USERS)
                                        .document(otherUserDocumentId)
                                        .collection(Constants.FRIENDSHIPS)
                                        .document(friendship1.documentId)
                                        .update(updateMap)
                                }
                            }
                        }
                }
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
            FirebaseFirestore.getInstance()
                .collection(roomId + "_typing")
                .add(TypingUser("", myUsername))
        }
    }

    private fun sendNotTypingStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance()
                .collection(roomId + "_typing")
                .whereEqualTo("username", myUsername)
                .get()
                .addOnSuccessListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val typingUsers = it.toObjects(TypingUser::class.java)
                        try {
                            if (typingUsers != null) {
                                FirebaseFirestore.getInstance()
                                    .collection(roomId + "_typing")
                                    .document(typingUsers[0].documentID)
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
        addLastMessageToFriendShip(otherUsername)
        super.finish()
    }

    override fun onPause() {
        sendNotTypingStatus()
        super.onPause()
    }

    override fun onBackPressed() {
        Utils.showDialog(this, object : AlertDialogCallbacks {
            override fun onPositiveButtonClicked(string: String) {
                finish()
            }

            override fun onNegativeButtonClicked() {}
        }, "Do you really want to exit?")
    }

    @SuppressLint("HardwareIds")
    private fun sendMessage() {
        isFirstTime = false
        val msg = binding.messageEditText.text.toString().trim()
        val deviceDetail = Utils.getDeviceDetail(this)
        val message = Message(
            "",
            currentIndex + 1,
            "",
            msg,
            deviceDetail,
            myUsername,
            Utils.getMessageTime(),
            System.currentTimeMillis(),
            Utils.getDate()
        )

        lifecycleScope.launch(Dispatchers.IO) {
            FirebaseFirestore.getInstance()
                .collection(roomId)
                .add(message)
                .addOnSuccessListener {
                    binding.sendBtn.isEnabled = false
                }
                .addOnFailureListener {
                    binding.messageEditText.setText(msg)
                }
        }

        lastMessage = message

        binding.messageEditText.setText("")

    }

}