package com.arhamsoft.matchranker.models

data class RejectedResponse(
    val httpCode: Long,
    val token: String?,
    val detail: String?,
    val data: Any?,
    val isValidated: Boolean,
)
