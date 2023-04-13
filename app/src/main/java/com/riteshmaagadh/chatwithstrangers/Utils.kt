package com.riteshmaagadh.chatwithstrangers

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.snackbar.Snackbar
import com.google.type.DateTime
import com.riteshmaagadh.chatwithstrangers.callbacks.AlertDialogCallbacks
import com.riteshmaagadh.chatwithstrangers.databinding.InfoDialogBgBinding
import com.riteshmaagadh.chatwithstrangers.databinding.SkipUserDialogBinding
import com.riteshmaagadh.chatwithstrangers.databinding.VerificationDialogBinding
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.text.DateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt


object Utils {

    fun isSubscriber(context: Context): Boolean =
        context.getSharedPreferences("app_constants", Context.MODE_PRIVATE)
            .getBoolean("is_subscriber", false)

    fun makeStatusBarBlack(activity: Activity){
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.statusBarColor = activity.getColor(R.color.dark)
            activity.window.navigationBarColor = activity.getColor(R.color.dark)
        }
    }

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

    fun showDialog(context: Context, alertDialogCallbacks: AlertDialogCallbacks, titleTxt: String) {
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

    fun showVerificationDialog(context: Context, alertDialogCallbacks: AlertDialogCallbacks, titleTxt: String) {
        val dialog = Dialog(context)
        val binding = VerificationDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawableResource(R.drawable.verificaion_dialog_bg)
        dialog.setCancelable(false)

        val layoutParams = dialog.window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = layoutParams

        binding.otpEditText.addTextChangedListener {
            binding.yesBtn.isEnabled = it.toString().length == 4
        }

        binding.titleTv.text = titleTxt

        binding.yesBtn.setOnClickListener {
            dialog.dismiss()
            alertDialogCallbacks.onPositiveButtonClicked(binding.otpEditText.otpValue.toString())
        }

        binding.noBtn.setOnClickListener {
            dialog.dismiss()
            alertDialogCallbacks.onNegativeButtonClicked()
        }

        dialog.show()
    }

    fun showInfoDialog(context: Context, alertDialogCallbacks: AlertDialogCallbacks, titleTxt: String) {
        val dialog = Dialog(context)
        val binding = InfoDialogBgBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawableResource(R.drawable.verificaion_dialog_bg)
        dialog.setCancelable(false)

        val layoutParams = dialog.window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = layoutParams

        binding.titleTv.text = titleTxt

        binding.yesBtn.setOnClickListener {
            dialog.dismiss()
            alertDialogCallbacks.onPositiveButtonClicked()
        }

        dialog.show()
    }
    fun showSnackBar(view: View, text: String) {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
    }

    fun getDeviceDetail(activity: Activity) : String {
        val displayMetrics: DisplayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val defaultDisplay =
                DisplayManagerCompat.getInstance(activity).getDisplay(Display.DEFAULT_DISPLAY)
            val displayContext = activity.createDisplayContext(defaultDisplay!!)
            displayMetrics = displayContext.resources.displayMetrics
        } else {
            displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        }

        val x = (displayMetrics.widthPixels / displayMetrics.xdpi).toDouble().pow(2.0);
        val y = (displayMetrics.heightPixels / displayMetrics.ydpi).toDouble().pow(2.0);

        val screenInches = sqrt(x + y)

        val deviceId = Settings.Secure.getString(
            activity.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val deviceDetail = JSONObject()
        deviceDetail.put("screen_inches", screenInches)
        deviceDetail.put("device_id", deviceId)
        deviceDetail.put("manufacturer", Build.MANUFACTURER)
        deviceDetail.put("ip_address", getDeviceIpAddress())
        deviceDetail.put("brand", Build.BRAND)
        deviceDetail.put("model", Build.MODEL)
        deviceDetail.put("board", Build.BOARD)
        deviceDetail.put("hardware", Build.HARDWARE)
        deviceDetail.put("serial", Build.SERIAL)
        deviceDetail.put("bootloader", Build.BOOTLOADER)
        deviceDetail.put("user", Build.USER)
        deviceDetail.put("host", Build.HOST)
        deviceDetail.put("release", Build.VERSION.RELEASE)
        deviceDetail.put("sdk_int", Build.VERSION.SDK_INT)
        deviceDetail.put("build_id", Build.ID)
        deviceDetail.put("build_time", Build.TIME)
        deviceDetail.put("fingerprint", Build.FINGERPRINT)

        return deviceDetail.toString()
    }

    private fun getDeviceIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress() && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    fun getDate() : String = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(Date());

    fun getMessageTime() : String = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(Date())

    const val PRIVACY_POLICY_URL = "https://mungiatalktostrangers.blogspot.com/2023/01/privacy-policy.html"

}