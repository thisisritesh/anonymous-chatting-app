package com.riteshmaagadh.chatwithstrangers

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.riteshmaagadh.chatwithstrangers.databinding.FragmentDiscoverBinding
import com.riteshmaagadh.chatwithstrangers.models.Friendship
import com.riteshmaagadh.chatwithstrangers.models.User
import kotlin.random.Random

class DiscoverFragment : Fragment() {

    private lateinit var binding: FragmentDiscoverBinding
    private lateinit var myUsername: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        initUi()
        return binding.root
    }

    private fun initUi() {
        myUsername = Pref(requireContext()).getPrefString(Constants.USERNAME)

        binding.checkBox.setOnCheckedChangeListener { compoundButton, b ->
            binding.signUpBtn.isEnabled = b
        }

        binding.signUpBtn.setOnClickListener {
            it.visibility = View.GONE
            binding.explanationUi.visibility = View.GONE
            binding.searchingParentUi.visibility = View.VISIBLE
            binding.searchingAnimation.playAnimation()

            FirebaseFirestore.getInstance()
                .collection(Constants.USERS)
                .whereNotEqualTo("username", myUsername)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val users = querySnapshot.toObjects(User::class.java)
                    if (!users.isNullOrEmpty()) {
                        startSearch(users, myUsername)
                    } else {
                        Toast.makeText(requireContext(), "Try again!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun startSearch(otherUsers: List<User>, username: String) {
        var position = 0
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(otherUsers[position].documentId)
            .collection("friendships")
            .whereEqualTo("friend_username", username)
            .get()
            .addOnSuccessListener {
                val friendships = it.toObjects(Friendship::class.java)
                if (friendships.isNullOrEmpty()) {
                    // send friend request
                    startChatActivity(otherUsers[position])
                } else {
                    position += 1
                    startSearch(otherUsers, username)
                }
            }
    }

    private fun startChatActivity(otherUser: User) {
        binding.searchingAnimation.pauseAnimation()
        binding.searchingParentUi.visibility = View.GONE
        binding.signUpBtn.visibility = View.VISIBLE
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra(Constants.IS_FROM_CHATS, false)
        intent.putExtra(Constants.OTHER_USERNAME, otherUser.username)
        intent.putExtra(Constants.OTHER_USER_PROFILE_PIC, otherUser.avatar_url)
        intent.putExtra(Constants.OTHER_USER_DOCUMENT_ID, otherUser.documentId)
        intent.putExtra(Constants.ROOM_ID, "")
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DiscoverFragment()
    }
}