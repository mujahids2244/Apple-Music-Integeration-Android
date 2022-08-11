package com.arhamsoft.matchranker.models

import java.io.Serializable

data class SongCheckData (
    var points: Double? = 1000.00,
    var songTitle: String? =null,
    var position: Double? = 1.0,
    var artistTitle: String? = null,
    var duration: Double? = 0.0,
    var probablity: Double? = 1.0,
    var kFactor: Long? = 40,
    var isRejected: Boolean? = false,
    var image: String? = null,
    var playCount: Long?=0,
    var songCode: String?=null,
    var rowNo: Long?=0,
    val total: Long?=0
) : Serializable