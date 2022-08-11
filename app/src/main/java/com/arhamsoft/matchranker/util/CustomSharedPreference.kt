package com.arhamsoft.matchranker.util

import android.content.Context
import android.content.SharedPreferences
import com.arhamsoft.matchranker.activity.LogIn
import com.arhamsoft.matchranker.room.User
import com.arhamsoft.matchranker.usermodels.LoginData
import com.google.gson.Gson

class CustomSharedPreference(context: Context) {
    private val PREFS_NAME = "MatchRanker"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPref.edit()

    fun saveLogin(KEY_NAME: String, value: Boolean) {

        editor.putBoolean(KEY_NAME, value)

        editor.commit()
    }
//    fun saveLoginData(KEY_NAME: String, obj: User) {
//
//        val dsave = sharedPref.edit()
//
//        val gson = Gson()
//
//        val json = gson.toJson(obj)
//
//        dsave.putString(KEY_NAME,json)
//        dsave.apply()
//    }

    fun isLogin(KEY_NAME: String): Boolean {

        return sharedPref.getBoolean(KEY_NAME, false)
    }


    fun clearSharedPreference() {

        editor.clear()
        editor.commit()
    }
}