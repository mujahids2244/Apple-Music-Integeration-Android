package com.arhamsoft.matchranker.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class APIResult<T>(handler: ApiHandler, call: Call<T>) {
    init {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {

                if (response.body() != null)
                    handler.onSuccess(response.body() as Any)
                else handler.onFailure(Throwable())
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                handler.onFailure(t)
            }
        })
    }
}