package com.arhamsoft.matchranker.models

data class PlayedSearchModelResultSongs(
    val href: String,
    val next: String,
    val data: List<PlayedDataModel>?
)
