package com.arhamsoft.matchranker.models

data class AddUserModel(
    val httpCode: Long,
    val token: Any? = null,
    val detail: String,
    val data: List<AddUserModelData>,
    val isValidated: Boolean,
    val json: Any? = null,
    val error: Any? = null,
    val isExpire: Boolean
)
