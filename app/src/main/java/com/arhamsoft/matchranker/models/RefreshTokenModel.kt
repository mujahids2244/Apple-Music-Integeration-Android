package com.arhamsoft.matchranker.models

data class RefreshTokenModel(
    val httpCode: Long,
    val token: Any? = null,
    val detail: Any? = null,
    val data: RefreshTokenModelData,
    val isValidated: Boolean,
    val json: Any? = null,
    val error: Any? = null,
    val isExpire: Boolean
)