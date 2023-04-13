package com.riteshmaagadh.chatwithstrangers.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.riteshmaagadh.chatwithstrangers.Constants
import com.riteshmaagadh.chatwithstrangers.FullImageActivity
import com.riteshmaagadh.chatwithstrangers.databinding.MessageItemMeBinding
import com.riteshmaagadh.chatwithstrangers.databinding.MessageItemOtherBinding
import com.riteshmaagadh.chatwithstrangers.databinding.MyMediaMessageItemBinding
import com.riteshmaagadh.chatwithstrangers.databinding.OtherMediaMessageItemBinding
import com.riteshmaagadh.chatwithstrangers.models.Message

class MessagesAdapter(private val messages: List<Message>, private val myUsername: String, private val activity: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    inner class OtherMessageViewHolder(private val binding: MessageItemOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, isSameDate: Boolean) {
            binding.messageTextView.text = messages[position].message
            binding.timestampTextView.text = messages[position].timestamp

            if (isSameDate) {
                binding.dateTextView.visibility = View.GONE
            } else {
                binding.dateTextView.text = messages[position].date
                binding.dateTextView.visibility = View.VISIBLE
            }

        }
    }

    inner class MyMessageViewHolder(private val binding: MessageItemMeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, isSameDate: Boolean) {
            binding.messageTextView.text = messages[position].message
            binding.timestampTextView.text = messages[position].timestamp

            if (isSameDate) {
                binding.dateTextView.visibility = View.GONE
            } else {
                binding.dateTextView.text = messages[position].date
                binding.dateTextView.visibility = View.VISIBLE
            }

        }
    }

    inner class MyMediaMessageViewHolder(private val binding: MyMediaMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, isSameDate: Boolean) {
            Glide.with(binding.imageView.context)
                .load(messages[position].media_url)
                .into(binding.imageView)
            binding.timestampTextView.text = messages[position].timestamp

            if (isSameDate) {
                binding.dateTextView.visibility = View.GONE
            } else {
                binding.dateTextView.text = messages[position].date
                binding.dateTextView.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener {
                val intent = Intent(activity, FullImageActivity::class.java)
                intent.putExtra(Constants.IMAGE_URL, messages[position].media_url)
                activity.startActivity(intent)
            }

        }
    }

    inner class OtherMediaMessageViewHolder(private val binding: OtherMediaMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, isSameDate: Boolean) {
            Glide.with(binding.imageView.context)
                .load(messages[position].media_url)
                .into(binding.imageView)
            binding.timestampTextView.text = messages[position].timestamp

            if (isSameDate) {
                binding.dateTextView.visibility = View.GONE
            } else {
                binding.dateTextView.text = messages[position].date
                binding.dateTextView.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener {
                val intent = Intent(activity, FullImageActivity::class.java)
                intent.putExtra(Constants.IMAGE_URL, messages[position].media_url)
                activity.startActivity(intent)
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> {
                MyMediaMessageViewHolder(MyMediaMessageItemBinding.inflate(layoutInflater,parent,false))
            }
            2 -> {
                MyMessageViewHolder(MessageItemMeBinding.inflate(layoutInflater, parent,false))
            }
            3 -> {
                OtherMediaMessageViewHolder(OtherMediaMessageItemBinding.inflate(layoutInflater,parent,false))
            }
            4 -> {
                OtherMessageViewHolder(MessageItemOtherBinding.inflate(layoutInflater, parent,false))
            }
            else -> {MyMediaMessageViewHolder(MyMediaMessageItemBinding.inflate(layoutInflater,parent,false))}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sent_by == myUsername) {
            if (messages[position].media_url.isNotEmpty() && messages[position].message.isEmpty()) {
                1
            } else {
                2
            }
        } else {
            if (messages[position].media_url.isNotEmpty() && messages[position].message.isEmpty()) {
                3
            } else {
                4
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messages.isNotEmpty()) {
            var isSameDate = false
            try {
                 isSameDate = messages[position].date == messages[position - 1].date
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (messages[position].sent_by == myUsername) {
                if (messages[position].media_url.isNotEmpty() && messages[position].message.isEmpty()) {
                    (holder as MyMediaMessageViewHolder).bind(position, isSameDate)
                } else {
                    (holder as MyMessageViewHolder).bind(position, isSameDate)
                }
            } else {
                if (messages[position].media_url.isNotEmpty() && messages[position].message.isEmpty()) {
                    (holder as OtherMediaMessageViewHolder).bind(position, isSameDate)
                } else {
                    (holder as OtherMessageViewHolder).bind(position, isSameDate)
                }
            }
        }
    }

}