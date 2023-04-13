package com.riteshmaagadh.chatwithstrangers

import android.content.Context
import android.content.SharedPreferences

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class Pref(val context: Context) {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    public fun getPrefString(key: String) : String {
        return sharedPreferences.getString(key,"")!!
    }

    public fun putPref(key: String, value: String) {
        editor.putString(key,value)
        editor.commit()
    }

    public fun getPrefBoolean(key: String) : Boolean {
        return sharedPreferences.getBoolean(key,false)
    }

    public fun putPref(key: String, value: Boolean) {
        editor.putBoolean(key,value)
        editor.commit()
    }

    public fun getPrefInt(key: String) : Int {
        return sharedPreferences.getInt(key,0)
    }

    public fun putPref(key: String, value: Int) {
        editor.putInt(key,value)
        editor.commit()
    }

}
