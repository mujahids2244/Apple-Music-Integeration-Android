package com.arhamsoft.matchranker.models

data class WatchUserRecentactivity(
    val id: Long,
    val playerId: String,
    val playerName: String,
    val playerImage: String,
    val songCode: String,
    val songName: String,
    val artistTitle: String,
    val songImage: String,
    val commentId: Long,
    val action: Long,
    val createdDate: String
)
