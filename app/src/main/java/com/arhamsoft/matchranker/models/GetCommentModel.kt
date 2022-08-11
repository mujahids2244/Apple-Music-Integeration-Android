package com.arhamsoft.matchranker.models

data class GetCommentModel(
    val httpCode: Long,
    val token: Any? = null,
    val detail: String,
    val data: GetCommentData,
    val isValidated: Boolean,
    val json: Any? = null,
    val error: Any? = null,
    val isExpire: Boolean
)
