package com.mobile.spanduk.utils

import android.content.Context

class Preferences (val context: Context){

    companion object {
        const val PREF = "PREF"
    }

    val sharedPref = context.getSharedPreferences(PREF, 0)

    fun setValues(key: String, value: String){
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getValues(key: String): String? {
        return sharedPref.getString(key, "")
    }
}