package com.riteshmaagadh.chatwithstrangers

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.riteshmaagadh.chatwithstrangers.adapters.ChatAdapter
import com.riteshmaagadh.chatwithstrangers.databinding.FragmentChatsBinding
import com.riteshmaagadh.chatwithstrangers.models.Friendship
import com.riteshmaagadh.chatwithstrangers.models.Message
import com.riteshmaagadh.chatwithstrangers.models.User


class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        initUi()
        return binding.root
    }

    private fun initUi() {
        val myDocumentId = Pref(requireContext()).getPrefString(Constants.MY_DOCUMENT_ID)
//        FirebaseFirestore
//            .getInstance()
//            .collection(Constants.USERS)
//            .document(myDocumentId)
//            .collection(Constants.FRIENDSHIPS)
//            .get()
//            .addOnSuccessListener {
//                val friendships = it.toObjects(Friendship::class.java)
//                binding.chatsRecyclerView.adapter =
//                    ChatAdapter(friendships, object : ChatAdapter.AdapterCallback {
//                        override fun onChatItemClicked(friendship: Friendship) {
//                            val intent = Intent(requireContext(), ChatActivity::class.java)
//                            intent.putExtra(Constants.OTHER_USERNAME, friendship.friend_username)
//                            intent.putExtra(
//                                Constants.OTHER_USER_PROFILE_PIC,
//                                friendship.friend_profile_pic
//                            )
//                            intent.putExtra(Constants.IS_FROM_CHATS, true)
//                            intent.putExtra(Constants.ROOM_ID, friendship.room_id)
//                            intent.putExtra(Constants.OTHER_USER_DOCUMENT_ID, friendship.friend_document_id)
//                            startActivity(intent)
//                        }
//                    })
//            }

        FirebaseFirestore
            .getInstance()
            .collection(Constants.USERS)
            .document(myDocumentId)
            .collection(Constants.FRIENDSHIPS)
            .addSnapshotListener { value, error ->
                if (error == null) {
                    try {
                        val friendships = value?.toObjects(Friendship::class.java)!!
                        binding.chatsRecyclerView.adapter =
                            ChatAdapter(friendships, object : ChatAdapter.AdapterCallback {
                                override fun onChatItemClicked(friendship: Friendship) {
                                    val intent = Intent(requireContext(), ChatActivity::class.java)
                                    intent.putExtra(Constants.OTHER_USERNAME, friendship.friend_username)
                                    intent.putExtra(
                                        Constants.OTHER_USER_PROFILE_PIC,
                                        friendship.friend_profile_pic
                                    )
                                    intent.putExtra(Constants.IS_FROM_CHATS, true)
                                    intent.putExtra(Constants.ROOM_ID, friendship.room_id)
                                    intent.putExtra(Constants.OTHER_USER_DOCUMENT_ID, friendship.friend_document_id)
                                    startActivity(intent)
                                }
                            })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }


    }


    companion object {
        @JvmStatic
        fun newInstance(): ChatsFragment = ChatsFragment()
    }

}