package com.arhamsoft.matchranker.models

data class UserActivityModel(
    val httpCode: Long,
    val token: Any? = null,
    val detail: String,
    val data: List<UserActivityModelData>?,
    val isValidated: Boolean,
    val json: Any? = null,
    val error: Any? = null,
    val isExpire: Boolean
)