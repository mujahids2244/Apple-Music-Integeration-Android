package com.arhamsoft.matchranker.util

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData

class InternetConLiveData(private val connectivityManager: ConnectivityManager):LiveData<Boolean>() {


    constructor(context: Context): this(
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)


    val networkCallback = object : ConnectivityManager.NetworkCallback(){

//        override fun onAvailable(network: Network) {
//            super.onAvailable(network)
//            postValue(true)
//        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }
    }


    override fun onActive() {
        super.onActive()
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)

    }
}