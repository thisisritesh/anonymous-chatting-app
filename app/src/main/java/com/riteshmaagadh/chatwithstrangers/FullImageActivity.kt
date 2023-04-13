package com.riteshmaagadh.chatwithstrangers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.riteshmaagadh.chatwithstrangers.databinding.ActivityFullImageBinding

class FullImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.makeStatusBarBlack(this)
        binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val imageUrl = intent.extras?.getString(Constants.IMAGE_URL)!!

        Glide.with(this)
            .load(imageUrl)
            .into(binding.imageView)

        binding.skipBtn.setOnClickListener {
            finish()
        }

    }
}