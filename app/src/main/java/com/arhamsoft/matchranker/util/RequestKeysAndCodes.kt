package com.arhamsoft.matchranker.util

import com.arhamsoft.matchranker.interfaces.PassThePositionOfPlayingSong
import com.arhamsoft.matchranker.interfaces.PassThePositionOfPlayingSongFromSearch
import com.arhamsoft.matchranker.models.PlayedDataModel
import com.arhamsoft.matchranker.models.SongCheckData

object RequestKeysAndCodes {

    const val songListData: String = "SongsList"
    lateinit var songListPlayer: ArrayList<SongCheckData>
    lateinit var songListPlayerSearch: ArrayList<PlayedDataModel>
    var passPositionToFragment: PassThePositionOfPlayingSong? = null

    const val songListDataPosition: String = "SongsListPosition"
    var position: Int = 0
    var positionSearch: Int = 0

    lateinit var songList: ArrayList<String>
    lateinit var songListSearch: ArrayList<String>
    lateinit var passThePositionOfPlayingSong: PassThePositionOfPlayingSong

    var passThePositionOfPlayingSongSearch: PassThePositionOfPlayingSongFromSearch? = null


    var isPlayingFromMatchSongs: Boolean = false
    var isPlayingFromSearch: Boolean = false
    var isPlayingFromRankSongs: Boolean = false
    var isPlayingFromDetails: Boolean = false

    fun setPassThePositionObject(passThePositionOfPlayingSong: PassThePositionOfPlayingSong) {
        this.passThePositionOfPlayingSong = passThePositionOfPlayingSong
    }

    fun upDateThePosition(position: Int) {
        passThePositionOfPlayingSong.positionOfCurrentSong(position)
    }

    fun setPassToFragmentInterface(passPositionToFragment: PassThePositionOfPlayingSong?) {
        this.passPositionToFragment = passPositionToFragment
    }

    fun passingToFragment(position: Int) {
        if(passPositionToFragment != null) {
            passPositionToFragment!!.positionOfCurrentSong(position)
        }
    }

    fun setPassThePositionObjectSearch(passThePositionOfPlayingSongFromSearch: PassThePositionOfPlayingSongFromSearch?) {
        this.passThePositionOfPlayingSongSearch = passThePositionOfPlayingSongFromSearch
    }

    fun upDateThePositionSearch(position: Int) {
        if (passThePositionOfPlayingSongSearch != null) {
            passThePositionOfPlayingSongSearch!!.positionOfCurrentSongSearch(position)
        }
    }
}