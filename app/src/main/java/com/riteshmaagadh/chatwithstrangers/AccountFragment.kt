package com.riteshmaagadh.chatwithstrangers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.riteshmaagadh.chatwithstrangers.callbacks.AlertDialogCallbacks
import com.riteshmaagadh.chatwithstrangers.databinding.FragmentAccountBinding
import com.riteshmaagadh.chatwithstrangers.models.Friendship
import com.riteshmaagadh.chatwithstrangers.models.User

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater,container,false)
        initUi()
        return binding.root
    }

    private fun initUi() {
        val packageName = requireActivity().packageName

        binding.rateUsParent.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
            }
        }

        binding.moreAppsParent.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:"+ "Alien Apps Labs")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:"+ "Alien Apps Labs")))
            }
        }

        binding.privacyPolicyParent.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(Utils.PRIVACY_POLICY_URL)
            startActivity(i)
        }

        binding.logOutBtn.setOnClickListener {
            Utils.showDialog(requireContext(), object : AlertDialogCallbacks {
                override fun onPositiveButtonClicked(string: String) {
                    val pref = Pref(requireContext())
                    pref.putPref(Constants.LOGIN_STATUS,false)
                    pref.putPref(Constants.USERNAME,"")
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }

                override fun onNegativeButtonClicked() {

                }
            }, "Do you really want to logout?")
        }

        binding.appLockParent.setOnClickListener {
            startActivity(Intent(requireContext(), AppLockActivity::class.java))
        }

        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(requireContext(), EditActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()

        val pref = Pref(requireContext())

        binding.usernameTextView.text = pref.getPrefString(Constants.USERNAME)
        binding.friendsCountTextView.text = pref.getPrefString(Constants.JOINED_DATE)
        binding.bioTextView.text = pref.getPrefString(Constants.BIO)

        val imageUrl = pref.getPrefString(Constants.MY_PROFILE_PIC)
        Glide.with(requireContext())
            .load(imageUrl)
            .into(binding.avatarProfileImageView)

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AccountFragment()
    }
}