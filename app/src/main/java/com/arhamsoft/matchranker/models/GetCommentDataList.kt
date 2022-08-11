package com.arhamsoft.matchranker.models

data class GetCommentDataList(
    val commentId: Long,
    val playerId: String,
    val comment: String,
    val playerName: String,
    val playerImage: String?,
    val numberOfLikes: Long,
    val parentId: Long,
    val createdDate: String,
    val isLiked: Boolean
)
