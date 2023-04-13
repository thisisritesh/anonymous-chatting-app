package com.riteshmaagadh.chatwithstrangers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.riteshmaagadh.chatwithstrangers.databinding.ChatOptionsBottomSheetBinding

class ChatOptionsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: ChatOptionsBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChatOptionsBottomSheetBinding.inflate(inflater, container, false)
        initUI()
        return binding.root
    }

    private fun initUI() {
        
    }


}