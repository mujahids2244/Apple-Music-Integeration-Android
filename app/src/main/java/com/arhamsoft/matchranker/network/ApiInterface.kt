package com.arhamsoft.matchranker.network

import com.arhamsoft.matchranker.models.*
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RegisterResponse
import com.google.gson.JsonObject
import org.bytedeco.javacpp.annotation.Raw

import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {



    @GET()
    fun getLibrarySongs(@Url offset:String): Call<PlayedModel>


    @GET
    fun getRecentlySongs(@Url offset:String): Call<PlayedModel>


    @GET( )
    fun getHeavyRotationSongs(@Url offset:String): Call<PlayedModel>



    @GET( )
    fun getUserPlaylists(@Url offset:String): Call<PlayedModel>

//    @Headers(
//        "Authorization: Bearer",
//        "Music-User-Token: "
//    )
    @GET( )
    fun getUserPlaylistsSongs(@Url offset: String): Call<PlayedModel>

    @GET( )
    fun getSongsFromAppleMusic(@Url songTitle: String): Call<PlayedSearchModel>

    @Raw
    @POST(URLs.register)
    fun createUser2(
        @Body data: JsonObject
    ): Call<RegisterResponse>

    @Raw
    @POST(URLs.login)
    fun loginUser(
        @Body data: JsonObject
    ): Call<RegisterResponse>

    @Raw
    @POST(URLs.forgetPassword)
    fun forgotUser(
        @Body data: JsonObject
    ): Call<RegisterResponse>


    @Raw
    @POST(URLs.getSongDetails)
    fun checkSongDetails(
        @Body data: JsonObject
    ): Call<SongCheck>

    @Raw
    @POST(URLs.logout)
    fun logoutUser(
        @Body data: JsonObject
    ): Call<RefreshTokenModel>

    @Raw
    @POST(URLs.refreshtoken)
    fun refreshToken(
        @Body data: JsonObject
    ): Call<RefreshTokenModel>

    @Raw
    @POST(URLs.getRankSongsUrl)
    fun getRankSongs(
        @Body data: JsonObject
    ): Call<RankSongCheck>

    @Raw
    @POST(URLs.searchRankSongs)
    fun searchRankSongs(
        @Body data: JsonObject
    ): Call<RankSongCheck>

    @Raw
    @POST(URLs.updateRejected)
    fun getRejectedResponse(
        @Body data: JsonObject
    ): Call<RejectedResponse>

    @Raw
    @POST(URLs.getRejectedSongs)
    fun getRejectedSongs(
        @Body data: JsonObject
    ): Call<RankSongCheck>

    @Raw
    @POST(URLs.getUserActivity)
    fun getUserActivity(
        @Body data: JsonObject
    ): Call<UserActivityModel>

    //same response model fro both reject and win lose on match songs screen
    @Raw
    @POST(URLs.winLoseStatus)
    fun getWinLoseResponse(
        @Body data: JsonObject
    ): Call<RejectedResponse>

    @Raw
    @POST(URLs.songDetails)
    fun getSongDetailResponse(
        @Body data: JsonObject
    ): Call<SongDetailModel>

    @Raw
    @POST(URLs.getComments)
    fun getCommentResponse(
        @Body data: JsonObject
    ): Call<GetCommentModel>

    @Raw
    @POST(URLs.postComments)
    fun postCommentResponse(
        @Body data: JsonObject
    ): Call<GetCommentModel>

    @Raw
    @POST(URLs.userprofile)
    fun userProfileResponse(
        @Body data: JsonObject
    ): Call<UserProfileModel>

    @Raw
    @POST(URLs.userprofileUpdate)
    fun userProfileUpdateResponse(
        @Body data: JsonObject
    ): Call<UserProfileModel>

    @Raw
    @POST(URLs.followuser)
    fun usersFollow(
        @Body data: JsonObject
    ): Call<FollowModel>

    @Raw
    @POST(URLs.searchUser)
    fun searchUsers(
        @Body data: JsonObject
    ): Call<AddUserModel>

    @Raw
    @POST(URLs.statusUser)
    fun userStatus(
        @Body data: JsonObject
    ): Call<FollowModel>

    @Raw
    @POST(URLs.watchUser)
    fun watchUser(
        @Body data: JsonObject
    ): Call<WatchUsersModel>

    @Raw
    @POST(URLs.watchUserRankSongsURL)
    fun getWatchUserRankSongs(
        @Body data: JsonObject
    ): Call<RankSongCheck>

    @Raw
    @POST(URLs.developerToken)
    fun getDeveloperToken(
//        @Body data: JsonObject
    ): Call<RefreshTokenModel>

    @Raw
    @POST(URLs.commentAction)
    fun getCommentAction(
        @Body data: JsonObject
    ): Call<CommentActionModel>

//    @FormUrlEncoded
//    @POST(URLs.register)
//    fun createUser(
//        @Field("UserName") UserName: String,
//        @Field("email") email:String,
//        @Field("password") password:String,
//        @Field("fullName") fullName:String,
//        @Field("isAppleMusicSubscribed") isAppleMusicSubscribed:Boolean
//    ): Call<RegisterResponse>


}
