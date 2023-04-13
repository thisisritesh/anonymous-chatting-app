package com.riteshmaagadh.chatwithstrangers.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.riteshmaagadh.chatwithstrangers.TimeUtils.toTimeAgo
import com.riteshmaagadh.chatwithstrangers.databinding.ChatItemBinding
import com.riteshmaagadh.chatwithstrangers.models.Friendship
import com.riteshmaagadh.chatwithstrangers.models.User

class ChatAdapter(private val list: List<Friendship>, private val callback: AdapterCallback) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    interface AdapterCallback {
        fun onChatItemClicked(friendship: Friendship)
    }

    inner class ChatViewHolder(binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameTv = binding.usernameTextView
        val lastMessageTv = binding.lastMessageTv
        val timestampTv = binding.lastMessageTimestampTv
        val profilePic = binding.chatProfilePicIv
        init {
            binding.root.setOnClickListener {
                callback.onChatItemClicked(list[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder =
        ChatViewHolder(ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        Glide.with(holder.profilePic.context)
            .load(list[position].friend_profile_pic)
            .into(holder.profilePic)

        holder.nameTv.text = list[position].friend_username
        holder.timestampTv.text = list[position].time_stamp_in_milliseconds.toTimeAgo()
        holder.lastMessageTv.text =
            if (list[position].last_message.isEmpty()) "Send first message to ${list[position].friend_username}" else list[position].last_message

    }

    override fun getItemCount(): Int = list.size

}