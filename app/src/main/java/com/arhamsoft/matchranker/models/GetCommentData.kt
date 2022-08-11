package com.arhamsoft.matchranker.models

data class GetCommentData(
    val comments: List<GetCommentDataList>,
    val commentResponse: GetCommentDataList
)
