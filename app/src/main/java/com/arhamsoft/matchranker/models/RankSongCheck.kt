package com.arhamsoft.matchranker.models

data class RankSongCheck(
    val httpCode: Long,
    val token: String?,
    val detail: String?,
    val data: List<SongCheckData>?,
    val isValidated: Boolean

)
