package com.riteshmaagadh.chatwithstrangers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.riteshmaagadh.chatwithstrangers.databinding.ActivityLoginBinding
import com.riteshmaagadh.chatwithstrangers.models.User

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.makeStatusBarBlack(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.loginBtn.setOnClickListener {
            if (areAllFieldsSatisfied()) {
                loginUser(
                    binding.usernameEditText.text.toString().trim().lowercase(),
                    binding.passwordEditText.text.toString().trim()
                )
            }
        }


    }

    private fun areAllFieldsSatisfied(): Boolean {
        var isSatisfied = false
        if (binding.usernameEditText.text!!.isNotEmpty()) {
            isSatisfied = true
        } else {
            binding.usernameEditText.error = "Enter username"
        }

        if (binding.passwordEditText.text!!.isNotEmpty()) {
            isSatisfied = true
        } else {
            binding.passwordEditText.error = "Enter password"
        }
        return isSatisfied
    }

    private fun loginUser(email: String, password: String) {
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {authResult ->
                val uid = authResult.user?.uid!!
                FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("uid",uid)
                    .get()
                    .addOnSuccessListener {
                        val users = it.toObjects(User::class.java)
                        if (users.isNotEmpty()) {
                            val user = users[0]
                            if (user.password == password) {
                                val pref = Pref(this)
                                pref.putPref(Constants.USERNAME, user.username)
                                pref.putPref(Constants.LOGIN_STATUS, true)
                                pref.putPref(Constants.MY_DOCUMENT_ID, user.documentId)
                                pref.putPref(Constants.MY_PROFILE_PIC, user.avatar_url)
                                pref.putPref(Constants.JOINED_DATE, user.joined_at)
                                pref.putPref(Constants.BIO, user.bio)
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Utils.showSnackBar(binding.root, "Wrong password")
                            }
                        } else {
                            Utils.showSnackBar(binding.root, "No account found with this username")
                        }
                    }
            }
            .addOnFailureListener {

            }
    }

}