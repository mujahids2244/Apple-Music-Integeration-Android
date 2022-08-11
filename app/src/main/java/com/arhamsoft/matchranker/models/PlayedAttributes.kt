package com.arhamsoft.matchranker.models



data class PlayedAttributes(
    val previews: List<Preview>,
    val artwork: PlayedArtwork,
    val artistName: String,
    val url: String,
    val discNumber: Long,
    val genreNames: List<String>,
    val durationInMillis: Long,
    val releaseDate: String,
    val name: String,
    val isrc: String,
    val hasLyrics: Boolean,
    val albumName: String,
    val playParams: PlayParams? = null,
    val trackNumber: Long,
    val composerName: String,
    val contentRating: String? = null
)