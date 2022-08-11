package com.arhamsoft.matchranker.models

data class AddUserModelData(
    val isFollowing: Boolean,
    val playerId: String,
    val profileImage: String? = null,
    val name: String,
    val isPublic: Boolean
)
