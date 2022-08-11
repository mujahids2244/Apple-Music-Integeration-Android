package com.arhamsoft.matchranker.models

data class UserActivityModelData(
    val id: Long,
    val playerId: String,
    val playerName: String,
    val playerImage: String? = null,
    val songCode: String? = null,
    val songName: String,
    val artistTitle: String,
    val songImage: String,
    val commentId: Long,
    val action: Long,
    val createdDate: String
)
