package com.arhamsoft.matchranker.models

data class WatchUsersModelData(
    val playerId: String,
    val profileImage: String,
    val name: String,
    val isFollowing: Boolean,
    val totalNoOfLikes: Long,
    val totalNoOfComments: Long,
    val followersCount: Long,
    val followingCount: Long,
    val recentactivity: List<WatchUserRecentactivity>
)
