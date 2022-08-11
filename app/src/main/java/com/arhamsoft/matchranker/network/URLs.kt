package com.arhamsoft.matchranker.network

import androidx.appcompat.app.AppCompatActivity
import com.arhamsoft.matchranker.models.*

object URLs {
    const val baseURL = "https://api.music.apple.com/"
    const val baseURLUser = "https://matchrankerapi.arhamsoft.net/"

    const val register = "api/auth/signup"
    const val login = "api/auth/login"
    const val logout = "api/auth/logout"
    const val forgetPassword = "api/auth/forgotpassword"
    const val resendEmail = "api/auth/resendVerificationemail"
    const val updateRejected = "api/match/updatesongs"
    const val getRejectedSongs = "api/match/getrejectedsongs"
    const val getUserActivity = "api/comment/getactivity"
    const val searchRankSongs = "api/player/searchsongs"
    const val winLoseStatus = "api/match/logstatus"
    const val songDetails = "api/match/getsongslog"
    const val refreshtoken = "api/auth/tokenrefresh"
    const val getComments = "api/comment/getcomments"
    const val postComments = "api/Comment/addcomment"
    const val userprofile = "api/user/getprofile"
    const val userprofileUpdate = "api/user/updateprofile"
    const val followuser = "api/player/getplayersbystatus"
    const val statusUser = "api/Player/userconnectivitystatus"
    const val watchUser = "api/player/getplayerprofile"
    const val watchUserRankSongsURL = "api/player/getplayerrankedsongs"
    const val searchUser = "api/Player/searchplayers"
    const val developerToken = "api/auth/getdevelopertoken"
    const val commentAction = "api/comment/addcommentaction"




    //
    const val getSongDetails = "api/match/getsongdetail"
    const val getRankSongsUrl = "api/match/getrankedsongs"
    const val searchFromAppleMusic = "v1/catalog/us/search?limit=25&types=songs&term="



    var songsArray: ArrayList<PlayedDataModel> = ArrayList()
    var song1:PlayedDataModel? = null
    var song2:PlayedDataModel? = null
    var leftSong:SongCheckData? = null
    var rightSong:SongCheckData? = null
    var winPoints: Double?= null
    var losePoints: Double?= null
    var winName:String?=null
    var loseName:String?=null
    var kFacA: Int = 0
    var kFacB: Int = 0
    var noOfFollow:Long = 0
    var noOfFollowing:Long =0
    var follStatusCheck:Int = 0
    var playerId:String?= null
    var watchUserResponse:WatchUsersModel? = null
    var watchUserActivityList:ArrayList<WatchUserRecentactivity> = ArrayList()
//    var watchUserRankSongsList:ArrayList<SongCheckData> = ArrayList()
    var userProfileData: UserProfileData? = null
    var rankCurrentSong: SongCheckData?= null
   var currentSong:SongCheckData?= null
    var fragCheck:Int = 0
    var callmethodofanotherfrag:Int = 0
    var matchupSongId:String? = null
    var matchupCheck:Int = 0
    var fromRank:Int = 0
    var matchupSongSearchApple:PlayedDataModel? = null





}