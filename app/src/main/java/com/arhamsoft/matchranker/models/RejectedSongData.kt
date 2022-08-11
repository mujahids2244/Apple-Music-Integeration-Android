package com.arhamsoft.matchranker.models

data class RejectedSongData(

    var points: Double = 0.0,
    var songTitle: String = "",
    var position: Double = 0.0,
    var artistTitle: String = "",
    var duration: Double = 0.0,
    var probability: Double = 0.0,
    var kFactor: Long = 0,
    var isRejected: Boolean = false,
    var image: String ="",
    var playCount: Long= 0,
    var songCode: String ="",
    val isWinner: Boolean = false,
    var userId: String = ""
)