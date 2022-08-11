package com.arhamsoft.matchranker.models

data class WatchUsersModel(
    val httpCode: Long,
    val token: Any? = null,
    val detail: String,
    val data: WatchUsersModelData,
    val isValidated: Boolean,
    val json: Any? = null,
    val error: Any? = null,
    val isExpire: Boolean
)
