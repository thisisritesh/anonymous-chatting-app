package com.riteshmaagadh.chatwithstrangers.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.riteshmaagadh.chatwithstrangers.R
import com.riteshmaagadh.chatwithstrangers.databinding.AvatarItemBinding

class AvatarsAdapter(
    private val list: List<String>,
    private val context: Context,
    private val callbacks: AdapterCallbacks
) :
    RecyclerView.Adapter<AvatarsAdapter.AvatarViewHolder>() {

    var lastClickedPosition = -1

    interface AdapterCallbacks {
        fun onAvatarClicked(imageUrl: String)
        fun onFirstImageLoad(imageUrl: String)
    }

    inner class AvatarViewHolder(binding: AvatarItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imageView = binding.avatarImageView
        val bgView = binding.bgView
        init {
            binding.root.setOnClickListener {
                lastClickedPosition = adapterPosition
                callbacks.onAvatarClicked(list[adapterPosition])
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder =
        AvatarViewHolder(
            AvatarItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        if (lastClickedPosition == position)
            holder.bgView.setBackgroundResource(R.drawable.selected_avatar_bg)
        else
            holder.bgView.setBackgroundResource(R.drawable.transparent_bg)
        Glide.with(context)
            .load(list[position])
            .into(holder.imageView)
        if (position == 0) {
            callbacks.onFirstImageLoad(list[position])
            if (lastClickedPosition == -1)
                holder.bgView.setBackgroundResource(R.drawable.selected_avatar_bg)
        }
    }

    override fun getItemCount(): Int = list.size

}