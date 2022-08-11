package com.arhamsoft.matchranker.models

data class CommentActionModel(
    val httpCode: Long,
    val token: Any? = null,
    val detail: String,
    val data: Any? = null,
    val isValidated: Boolean,
    val json: Any? = null,
    val error: Any? = null,
    val isExpire: Boolean
)
