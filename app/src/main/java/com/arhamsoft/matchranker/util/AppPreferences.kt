package com.arhamsoft.matchranker.util

import android.content.Context
import android.content.SharedPreferences


private var preferences: SharedPreferences? = null

public class AppPreferences(context: Context) {


    private val KEY_APPLE_MUSIC_USER_TOKEN = "apple-music-user-token"
    private val DEVELOPER_TOKEN = "developer-token"

    private var instance: AppPreferences? = null


    fun with(context: Context?): AppPreferences? {
        if (AppPreferences.instance == null) {
            synchronized(AppPreferences::class.java) {
                AppPreferences.instance = AppPreferences(context!!)
            }
        }
        return AppPreferences.instance
    }

    fun getAppleMusicUserToken(): String? {
        return preferences!!.getString(KEY_APPLE_MUSIC_USER_TOKEN, null)
    }

    fun setAppleMusicUserToken(userToken: String?) {
        preferences!!.edit().putString(KEY_APPLE_MUSIC_USER_TOKEN, userToken).apply()
    }


    fun getDeveloperToken(): String? {
        return preferences!!.getString(DEVELOPER_TOKEN, null)
    }

    fun setDeveloperToken(dev_token: String?) {
        preferences!!.edit().putString(DEVELOPER_TOKEN, dev_token).apply()
    }

//    private fun AppPreferences(context: Context): AppPreferences? {
//        preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
//
//        return AppPreferences(context)
//    }



    companion object {
        private var instance: AppPreferences? = null
        private val PREFERENCES_FILE_NAME = "app_preferences"
        fun getInstance(context: Context): AppPreferences {
            if (instance == null) {
                preferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
                instance = AppPreferences(context)
            }
            return instance!!
        }
    }
}