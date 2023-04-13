package com.riteshmaagadh.chatwithstrangers

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.riteshmaagadh.chatwithstrangers.adapters.AvatarsAdapter
import com.riteshmaagadh.chatwithstrangers.databinding.ActivitySignUpBinding
import com.riteshmaagadh.chatwithstrangers.models.User
import com.riteshmaagadh.chatwithstrangers.models.OnlineUser

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private var onlineUserList: List<OnlineUser> = listOf()
    private var isUsernameSatisfied = false
    private var isBioSatisfied = false
    private var isPasswordSatisfied = false

    private fun areAllFieldsSatisfied(): Boolean =
        isUsernameSatisfied && isPasswordSatisfied

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.makeStatusBarBlack(this)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.usernameEditText.addTextChangedListener(usernameTextWatcher)
        binding.passwordEditText.addTextChangedListener(passwordTextWatcher)

        binding.signUpBtn.setOnClickListener {

            if (areAllFieldsSatisfied()) {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        binding.usernameEditText.text.toString(),
                        binding.passwordEditText.text.toString()
                    )
                    .addOnSuccessListener {
                        val uid = it.user?.uid!!
//                        val user = User(
//                            "",
//                            avatarUrl,
//                            binding.usernameEditText.text.toString().trim().lowercase(),
//                            binding.bioEditText.text.toString().trim(),
//                            binding.passwordEditText.text.toString().trim(),
//                            "",
//                            Utils.getDeviceDetail(this),
//                            Utils.getDate()
//                        )
//                        val user = User("","",it.user.email,)
                        createAccount(user)
                    }
                    .addOnFailureListener {

                    }
            }

        }

        binding.loginInsteadTxt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        FirebaseFirestore.getInstance()
            .collection("usernames")
            .addSnapshotListener { value, error ->
                if (error == null) {
                    onlineUserList = value?.toObjects(OnlineUser::class.java)!!
                }
            }

    }


    private val usernameTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            isUsernameSatisfied = p0?.isNotEmpty()!!
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    private val passwordTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (p0.toString().length < 6) {
                binding.passwordEditText.error = "Password must be at least of 6 characters!"
                isPasswordSatisfied = false
            } else {
                binding.passwordEditText.error = null
                isPasswordSatisfied = true
            }
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    private fun createAccount(user: User) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .add(user)
            .addOnSuccessListener {
                val pref = Pref(this)
                pref.putPref(Constants.LOGIN_STATUS, true)
                pref.putPref(Constants.USERNAME, user.username)
                pref.putPref(Constants.MY_DOCUMENT_ID, it.id)
                pref.putPref(Constants.MY_PROFILE_PIC, user.avatar_url)
                pref.putPref(Constants.JOINED_DATE, user.joined_at)
                pref.putPref(Constants.BIO, user.bio)
                FirebaseFirestore.getInstance()
                    .collection("usernames")
                    .add(OnlineUser(user.username))
                    .addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
            }
    }


}