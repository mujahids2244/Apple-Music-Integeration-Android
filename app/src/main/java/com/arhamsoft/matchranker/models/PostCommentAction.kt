package com.arhamsoft.matchranker.models

data class PostCommentAction(
    val userId: String="",
    val commentId: Long=0,
    val action: Long=0,
    val songCode: String="",
    val songName: String="",
    val songImage: String="",
    val artistTitle: String=""


)
