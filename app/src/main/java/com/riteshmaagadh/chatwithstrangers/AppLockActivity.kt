package com.riteshmaagadh.chatwithstrangers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.riteshmaagadh.chatwithstrangers.callbacks.AlertDialogCallbacks
import com.riteshmaagadh.chatwithstrangers.databinding.ActivityAppLockBinding

class AppLockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.makeStatusBarBlack(this)
        binding = ActivityAppLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = Pref(this)

        val isEnabled = pref.getPrefBoolean(Constants.IS_APP_LOCK_ENABLED)

        binding.switch1.isChecked = isEnabled

        if (isEnabled) {
            binding.changePinParent.visibility = View.VISIBLE
        } else {
            binding.changePinParent.visibility = View.GONE
        }

        addListenerToSwitch()

        binding.changePinBtn.setOnClickListener {
            val password = pref.getPrefString(Constants.APP_LOCK_PASSWORD)
            Utils.showVerificationDialog(this, object : AlertDialogCallbacks {
                override fun onPositiveButtonClicked(string: String) {
                    if (string == password) {
                        Utils.showVerificationDialog(this@AppLockActivity, object : AlertDialogCallbacks {
                            override fun onPositiveButtonClicked(string: String) {
                                pref.putPref(Constants.APP_LOCK_PASSWORD, string)
                                Utils.showSnackBar(binding.root, "Password changed!")
                            }

                            override fun onNegativeButtonClicked() {

                            }
                        }, "Set a new password!")
                    } else {
                        Utils.showSnackBar(binding.root, "Wrong password, Try again!")
                    }
                }

                override fun onNegativeButtonClicked() {

                }
            }, "Enter your PIN")
        }

        binding.closeBtn.setOnClickListener {
            finish()
        }

    }

    private fun verifyAndDisableAppLock() {
        binding.switch1.setOnCheckedChangeListener(null)
        val pref = Pref(this)
        Utils.showVerificationDialog(this, object : AlertDialogCallbacks {
            override fun onPositiveButtonClicked(string: String) {
                val password = pref.getPrefString(Constants.APP_LOCK_PASSWORD)
                if (string == password) {
                    pref.putPref(Constants.IS_APP_LOCK_ENABLED, false)
                    pref.putPref(Constants.APP_LOCK_PASSWORD,"")
                    binding.switch1.isChecked = false
                    binding.changePinParent.visibility = View.GONE
                } else {
                    Utils.showInfoDialog(this@AppLockActivity, object : AlertDialogCallbacks {
                        override fun onPositiveButtonClicked(string: String) {}

                        override fun onNegativeButtonClicked() {}
                    }, "Incorrect PIN")
                    binding.switch1.isChecked = true
                }
                addListenerToSwitch()
            }

            override fun onNegativeButtonClicked() {
                binding.switch1.isChecked = true
                addListenerToSwitch()
            }

        }, "Enter PIN to verify!")
    }


    private fun setANewPin() {
        val pref = Pref(this)
        binding.switch1.setOnCheckedChangeListener(null)
        Utils.showVerificationDialog(this, object : AlertDialogCallbacks {
            override fun onPositiveButtonClicked(string: String) {
                pref.putPref(Constants.APP_LOCK_PASSWORD, string)
                pref.putPref(Constants.IS_APP_LOCK_ENABLED, true)
                Utils.showInfoDialog(this@AppLockActivity, object : AlertDialogCallbacks {
                    override fun onPositiveButtonClicked(string: String) {
                        finish()
                    }

                    override fun onNegativeButtonClicked() {

                    }

                }, "App lock enabled!")
            }
            override fun onNegativeButtonClicked() {
                binding.switch1.isChecked = false
                addListenerToSwitch()
            }
        }, "Set a PIN")
    }

    private fun addListenerToSwitch() {
        binding.switch1.setOnCheckedChangeListener { p0, isChecked ->
            if (isChecked) {
                setANewPin()
            } else {
                verifyAndDisableAppLock()
            }
        }
    }

}
