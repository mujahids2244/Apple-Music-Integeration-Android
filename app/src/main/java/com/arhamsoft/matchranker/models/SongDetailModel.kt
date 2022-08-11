package com.arhamsoft.matchranker.models


data class SongDetailModel(
    val httpCode: Long,
    val token: String?,
    val detail: String?,
    val data: List<SongDetail>?,
    val isValidated: Boolean
)