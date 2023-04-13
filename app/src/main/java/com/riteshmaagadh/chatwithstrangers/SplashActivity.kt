package com.riteshmaagadh.chatwithstrangers

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Utils.makeStatusBarBlack(this)

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            if (Pref(this).getPrefBoolean(Constants.LOGIN_STATUS)) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 2000)

    }
}