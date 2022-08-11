package com.arhamsoft.matchranker.models

data class EditProfilePost(
    val userId: String ="",
    val fullName: String="",
    val profileImage: String? = null,
    val isPublic: Boolean = true
)
