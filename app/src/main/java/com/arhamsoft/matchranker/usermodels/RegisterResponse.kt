package com.arhamsoft.matchranker.usermodels

data class RegisterResponse(
    val httpCode: Long,
    val token: String?,
    val detail: String?,
    val data: LoginData,
    val isValidated: Boolean

)
