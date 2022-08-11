package com.arhamsoft.matchranker.network

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.util.AppPreferences
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(val context:Context) {

    private fun getHttpClientHeader(): OkHttpClient {
        val appleUserToken = AppPreferences.getInstance(context).getAppleMusicUserToken()
        val dev_token = AppPreferences.getInstance(context).getDeveloperToken()

        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $dev_token")
                .addHeader("Music-User-Token",appleUserToken!!)
                .build()
            chain.proceed(newRequest)
        }.build()
        return client
    }

    fun getRetrofitClient(): ApiInterface {
        return Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .client(getHttpClientHeader())
            .baseUrl(URLs.baseURL)
            .build()
            .create(ApiInterface::class.java)
    }
}