package com.arhamsoft.matchranker.models

data class PostCommentModel(
    var songCode: String ="",
    var comment: String ="",
    var userId: String ="",
    var songImage: String="",
    var songName: String="",
    var artistTitle: String="",
    var parentId: Long = 0
)
