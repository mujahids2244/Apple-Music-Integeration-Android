package com.arhamsoft.matchranker.fragment.matchSong

import androidx.lifecycle.ViewModel

class MatchSongsViewModel: ViewModel() {

    var songsModel1: SongsModel? = null

    fun setData1(songsModel: SongsModel) {
        this.songsModel1  = songsModel
    }


}