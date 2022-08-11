package com.arhamsoft.matchranker.models

data class SongCheck(
    val httpCode: Long,
    val token: String?,
    val detail: String?,
    var data: SongCheckData?,
    val isValidated: Boolean,
    val isExpire: Boolean

)
