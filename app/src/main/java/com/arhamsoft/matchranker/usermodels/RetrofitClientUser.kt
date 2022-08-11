package com.arhamsoft.matchranker.usermodels

import android.content.Context
import com.arhamsoft.matchranker.network.ApiInterface
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.UserDatabase
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClientUser(val context: Context) {


    private fun getHttpClientHeader(isForLogin:Boolean): OkHttpClient {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest: Request.Builder = chain.request().newBuilder()
                .addHeader("auth", "c2FtcGxldXNlcjE6QWRtaW5AMTIz")
//                .addHeader("token", user.token!!)
//                .build()
            if(!isForLogin) {
                val userlogin  = UserDatabase.getDatabase(context).userDao().getUser()

                userlogin?.token?.let {
                    if (!isForLogin && it.isNotEmpty()) {
                        newRequest.addHeader("token", it)
                    }
                }
            }


            chain.proceed(newRequest.build())
        }.build()
        return client
    }

    fun getRetrofitClientUser(isForLogin:Boolean): ApiInterface {
        return Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .client(getHttpClientHeader(isForLogin))
            .baseUrl(URLs.baseURLUser)
            .build()
            .create(ApiInterface::class.java)
    }
}