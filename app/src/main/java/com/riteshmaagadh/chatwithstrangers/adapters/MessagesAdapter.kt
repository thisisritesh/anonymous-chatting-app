package com.riteshmaagadh.chatwithstrangers.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.riteshmaagadh.chatwithstrangers.databinding.MessageItemMeBinding
import com.riteshmaagadh.chatwithstrangers.databinding.MessageItemOtherBinding
import com.riteshmaagadh.chatwithstrangers.models.Message

class MessagesAdapter(private val messages: List<Message>, private val deviceId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderDeviceId == deviceId) {
            0
        } else {
            1
        }
    }

    inner class OtherMessageViewHolder(private val binding: MessageItemOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.messageTextView.text = messages[position].message
        }
    }

    inner class MyMessageViewHolder(private val binding: MessageItemMeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.messageTextView.text = messages[position].message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            MyMessageViewHolder(MessageItemMeBinding.inflate(layoutInflater, parent,false))
        } else {
            OtherMessageViewHolder(MessageItemOtherBinding.inflate(layoutInflater, parent,false))
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messages[position].senderDeviceId == deviceId) {
            (holder as MyMessageViewHolder).bind(position)
        } else {
            (holder as OtherMessageViewHolder).bind(position)
        }
    }

}