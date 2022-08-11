package com.arhamsoft.matchranker.models

data class SongDetail(

    val songCode: String,
    val points: Double,
    val status: Boolean,
    val matchsongId: String,
    val songTitle: String,
    val artistTitle: String,
    val image: String,
    val createdDate: String
)
