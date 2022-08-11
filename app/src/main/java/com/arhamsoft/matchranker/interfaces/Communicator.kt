package com.arhamsoft.matchranker.interfaces

import com.arhamsoft.matchranker.models.PlayedDataModel
import com.arhamsoft.matchranker.models.SongCheckData

interface Communicator {
    fun passData(song: SongCheckData, position: Int = 0)

    fun passDataFromSearch(song:PlayedDataModel,position: Int= 0, check:Int)
}