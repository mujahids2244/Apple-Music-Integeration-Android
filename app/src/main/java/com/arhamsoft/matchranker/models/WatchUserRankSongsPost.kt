package com.arhamsoft.matchranker.models

data class WatchUserRankSongsPost (

    val userId:String = "",
    val playerID: String = "",
    val limit: Long = 0,
    val offset:Long = 0
)