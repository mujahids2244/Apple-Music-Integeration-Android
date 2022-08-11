package com.arhamsoft.matchranker.network

interface ApiHandler {
    fun onSuccess(response: Any)
    fun onFailure(t: Throwable)
}
