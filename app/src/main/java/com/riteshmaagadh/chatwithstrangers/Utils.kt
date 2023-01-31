package com.riteshmaagadh.chatwithstrangers

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.riteshmaagadh.chatwithstrangers.callbacks.AlertDialogCallbacks
import com.riteshmaagadh.chatwithstrangers.databinding.SkipUserDialogBinding
import com.riteshmaagadh.chatwithstrangers.databinding.TermsAndConditionsDialogBinding


object Utils {

    fun isSubscriber(context: Context): Boolean =
        context.getSharedPreferences("app_constants", Context.MODE_PRIVATE)
            .getBoolean("is_subscriber", false)

    fun setSubscriberEnabled(isSubscriber: Boolean, context: Context) {
        val sp = context.getSharedPreferences("app_constants", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean("is_subscriber", isSubscriber)
        editor.apply()
    }

    fun getUserDocumentId(context: Context): String =
        context.getSharedPreferences("app_constants", Context.MODE_PRIVATE)
            .getString("document_id", "")!!

    fun setUserDocumentId(documentId: String, context: Context) {
        val sp = context.getSharedPreferences("app_constants", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString("document_id", documentId)
        editor.apply()
    }

    fun showTermsDialog(context: Context, alertDialogCallbacks: AlertDialogCallbacks) {
        val dialog = Dialog(context)
        val binding = TermsAndConditionsDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.checkboxTerms.setOnCheckedChangeListener { compoundButton, b ->
            binding.startAChatBtn.isEnabled = b
        }

        binding.startAChatBtn.setOnClickListener {
            dialog.dismiss()
            alertDialogCallbacks.onPositiveButtonClicked()
        }

        dialog.show()
    }

    fun showSkipUserDialog(context: Context, alertDialogCallbacks: AlertDialogCallbacks, titleTxt: String) {
        val dialog = Dialog(context)
        val binding = SkipUserDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.titleTv.text = titleTxt

        binding.yesBtn.setOnClickListener {
            dialog.dismiss()
            alertDialogCallbacks.onPositiveButtonClicked()
        }

        binding.noBtn.setOnClickListener {
            dialog.dismiss()
            alertDialogCallbacks.onNegativeButtonClicked()
        }

        dialog.show()
    }


}