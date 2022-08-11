package com.arhamsoft.matchranker.models

data class UserProfileData(
    val fullName: String,
    val email: String,
    val isActive: Boolean,
    val isAdmin: Boolean,
    val userId: String,
    val username: String,
    val profileImage: String?,
    val isPublic: Boolean,
    val numberofFollowers: Long,
    val numberOfFollowings: Long
)
