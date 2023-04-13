package com.riteshmaagadh.chatwithstrangers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.riteshmaagadh.chatwithstrangers.adapters.AvatarsAdapter
import com.riteshmaagadh.chatwithstrangers.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var pref: Pref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.makeStatusBarBlack(this)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()

    }

    private fun initUI() {
        var avatarUrl = ""

        pref = Pref(this)
        binding.bioEditText.setText(pref.getPrefString(Constants.BIO))
        binding.bioEditText.setSelection(binding.bioEditText.length() - 1)
        binding.bioEditText.requestFocus()

        binding.closeBtn.setOnClickListener {
            finish()
        }

        binding.updateBtn.setOnClickListener {
            updateInfo(avatarUrl)
        }

        binding.avatarsRv.adapter =
            AvatarsAdapter(SampleData.getAvatars(), this, object : AvatarsAdapter.AdapterCallbacks {
                override fun onAvatarClicked(imageUrl: String) {
                    avatarUrl = imageUrl
                }

                override fun onFirstImageLoad(imageUrl: String) {
//                    avatarUrl = imageUrl
                }
            })

    }

    private fun updateInfo(avatarUrl: String) {
        val documentId = pref.getPrefString(Constants.MY_DOCUMENT_ID)

        val bio = binding.bioEditText.text.toString().trim()

        val updateMap: Map<String, Any> = mutableMapOf(
            Pair("bio", bio),
            Pair("avatar_url", avatarUrl)
        )

        pref.putPref(Constants.LOGIN_STATUS, true)
        pref.putPref(Constants.MY_PROFILE_PIC, avatarUrl)
        pref.putPref(Constants.BIO, bio)

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(documentId)
            .update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                finish()
            }
    }



}