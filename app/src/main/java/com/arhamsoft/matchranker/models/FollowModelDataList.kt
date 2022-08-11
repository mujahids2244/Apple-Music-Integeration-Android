package com.arhamsoft.matchranker.models

data class FollowModelDataList(
    val playerId: String,
    val profileImage: String? = null,
    val name: String,
    val isFollowing: Boolean,
)
