package com.arhamsoft.matchranker.activity

import android.content.DialogInterface
import android.content.Intent
import android.media.session.PlaybackState
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import coil.load
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.databinding.ActivityMainScreenBinding
import com.arhamsoft.matchranker.fragment.Account
import com.arhamsoft.matchranker.fragment.BottomSheetPlayer
import com.arhamsoft.matchranker.fragment.matchSong.MatchSongs
import com.arhamsoft.matchranker.fragment.RankSongs
import com.arhamsoft.matchranker.fragment.SongDetailsBottomDialog
import com.arhamsoft.matchranker.interfaces.CallBackViewLoader
import com.arhamsoft.matchranker.interfaces.Communicator
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.RetrofitClient
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.service.MediaBrowserHelperJava
import com.arhamsoft.matchranker.util.CustomSharedPreference
import com.arhamsoft.matchranker.util.InternetConLiveData
import com.arhamsoft.matchranker.interfaces.PassThePositionOfPlayingSong
import com.arhamsoft.matchranker.interfaces.PassThePositionOfPlayingSongFromSearch
import com.arhamsoft.matchranker.models.*
import com.arhamsoft.matchranker.room.User
import com.arhamsoft.matchranker.service.AppleMusicTokenProvider
import com.arhamsoft.matchranker.usermodels.LoginToAppleModel
import com.arhamsoft.matchranker.usermodels.RegisterResponse
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.arhamsoft.matchranker.util.AppPreferences
import com.arhamsoft.matchranker.util.RequestKeysAndCodes
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class MainScreen : AppCompatActivity(), Communicator, CallBackViewLoader,
    MediaBrowserHelperJava.Listener, PassThePositionOfPlayingSong,
    PassThePositionOfPlayingSongFromSearch {

    lateinit var binding: ActivityMainScreenBinding
    lateinit var sharedPreference: CustomSharedPreference
    lateinit var database: UserDatabase
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var recentList: List<PlayedDataModel>
    private lateinit var libraryList: List<PlayedDataModel>
    private var handler = Handler(Looper.getMainLooper())

    private lateinit var heavyList: List<PlayedDataModel>
    private lateinit var playList: List<PlayedDataModel>
    private var playListSongsId: ArrayList<String> = ArrayList()
    private var isCall: Boolean = false
    var pic: String? = null
    private var currentPlaybackState: PlaybackStateCompat? = null

    //        private var URLs.songsArray: ArrayList<String> = ArrayList()
    private var recentPlayUrl: String = "v1/me/recent/played/tracks?limit=30"
    private var userLibraryUrl: String = "v1/me/library/songs?limit=30"
    private var heavyRotateUrl: String = "v1/me/history/heavy-rotation?limit=10"
    private var userPlaylistUrl: String = "v1/me/library/playlists"
    private var userPlaylistSongsUrl: String = "v1/catalog/us/playlists/"
    lateinit var loading: LoadingDialog
    private var matchSongs: MatchSongs? = null
    private lateinit var connection: InternetConLiveData
    private lateinit var mediaBrowserHelper: MediaBrowserHelperJava
    private lateinit var mediaControllerCallBack: MediaControllerCallback
    private lateinit var mediaControllerPlayer: MediaControllerCompat
    lateinit var runnable: Runnable
    var u_id: String? = null
    private var bottemFragment: BottomSheetPlayer? = null
    private var songDetailFrag: SongDetailsBottomDialog? = null
    var u_token: String? = null

    companion object {
        lateinit var activity: AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activity = this
        database = UserDatabase.getDatabase(this)
        sharedPreference = CustomSharedPreference(this)
        loading = LoadingDialog(this)
        mediaBrowserHelper = MediaBrowserHelperJava(this, this)

        URLs.songsArray = ArrayList()


//        if (mediaBrowser != null && mediaBrowser.isConnected()) {
//            Bottomsheet_Player.onMediaBrowserConnected(mediaBrowser)
//        }

        recentList = ArrayList()
        libraryList = ArrayList()
        heavyList = ArrayList()
        playList = ArrayList()
        checkNetworkConnection()
        loading.startLoading("Please Wait")
//binding.lottieLoader.visibility = View.VISIBLE
        val devToken:String? = AppPreferences.getInstance(this).getDeveloperToken()

        GlobalScope.launch {
            val user = UserDatabase.getDatabase(this@MainScreen).userDao().getUser()
            u_id = user.userId
            u_token = user.token

            getdeveloperToken()
            callRecentPlayApi()
            callHeavyRotateApi()
            callUserPlaylistApi()
            callUserLibraryApi()
            callUserProfileDataApi()

//            refreshTokenApi()
        }

        binding.playerclick.setOnClickListener {
            bottemFragment = BottomSheetPlayer()
//            val intent = Intent(this, BottomSheetPlayer::class.java)
            bottemFragment!!.show(supportFragmentManager.beginTransaction(), "frag1")
            RequestKeysAndCodes.songListPlayer = songList
            RequestKeysAndCodes.songListPlayerSearch = songListSearch
            RequestKeysAndCodes.position = positionOfRank
            RequestKeysAndCodes.positionSearch = positionSearch
//            intent.putExtra(RequestKeysAndCodes.songListData, songList)
//            intent.putExtra(RequestKeysAndCodes.songListDataPosition, position)
//            startActivity(intent)
//            overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
        }

        binding.noint.tryAgainButton.setOnClickListener {

//            checkNetworkConnection()
            URLs.songsArray.clear()
            GlobalScope.launch {

                callRecentPlayApi()
                callHeavyRotateApi()
                callUserPlaylistApi()
                callUserLibraryApi()
                callUserProfileDataApi()

            }
            binding.noint.noInternet.visibility = View.GONE

        }

        binding.playNextSong.setOnClickListener {
            mediaControllerPlayer.transportControls.skipToNext()
        }

        //player progressbar
        runnable = Runnable {
            updateProgress()
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)

        binding.bottomNavigation.selectedItemId = R.id.song_match
        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.song_rank -> {
                    replaceFragment(RankSongs())

                }
                R.id.song_match -> {

//                    URLs.matchupCheck = 0
                    if (matchSongs == null) {
                        matchSongs = MatchSongs()
                        replaceFragment(matchSongs!!)
                    } else replaceFragment(matchSongs!!)

                }
                R.id.profilepg -> {
                    replaceFragment(Account())

                }
            }
            true
        })

        mediaBrowserHelper.connect()
    }

    private fun checkNetworkConnection() {

        connection = InternetConLiveData(this)

        connection.observe(this) { isConnected ->

            if (isConnected) {
                binding.noint.noInternet.visibility = View.GONE
            }
            else {
                binding.noint.noInternet.visibility = View.VISIBLE
            }
        }
    }

    private fun updateProgress() {
        if (currentPlaybackState == null) {
            return
        }
        var currentPosition = currentPlaybackState!!.position
        if (currentPlaybackState!!.state == PlaybackStateCompat.STATE_PLAYING) {
            val timeDelta =
                SystemClock.elapsedRealtime() - currentPlaybackState!!.lastPositionUpdateTime
            currentPosition += (timeDelta * currentPlaybackState!!.playbackSpeed).toLong()
        }
        binding.playerProgress.progress = currentPosition.toInt()
    }


    override fun onBackPressed() {
//        logoutAlertDialog()
    }

    private fun callFrag(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragContainer, fragment)
        fragmentTransaction.commit()
    }

    fun showFragment(fragment: SongDetailsBottomDialog) {
        songDetailFrag = fragment
        songDetailFrag!!.show(supportFragmentManager.beginTransaction(), "frag2")
    }


    private fun replaceFragment(fragment: Fragment) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        for (item in supportFragmentManager.fragments.indices) {
            if (supportFragmentManager.fragments[item].javaClass == fragment.javaClass) {
                val i = supportFragmentManager.fragments.size - item
                for (j in 1 until i) {
                    supportFragmentManager.popBackStack()
                }
                //supportFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                return
            }
        }

        fragmentTransaction.add(R.id.fragContainer, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        /*}else{
            fragmentTransaction.replace(R.id.fragContainer, fragment, tag)
            fragmentTransaction.commit()

            supportFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }*/

    }

    override fun passData(song: SongCheckData, position: Int) {
//        val intent = Intent(this@MainScreen, Bottomsheet_Player::class.java)
//        intent.putExtra("songId",song.songCode)
//        intent.putExtra("songObj", Gson().toJson(song))
//        startActivity(intent)
        /*this.position = position
        if (song.image.contains("{w}x{h}bb")) {
            pic = song.image.replace("{w}x{h}bb", "100x100bb")
        }
        binding.songPic.load(pic) {
            placeholder(R.drawable.ic_baseline_library_music_24)
        }
        binding.songTitle.text = song.songTitle
        binding.songArtist.text = song.artistTitle
        binding.musicplayer.visibility = View.VISIBLE*/

        if (position == -1) {
            RequestKeysAndCodes.isPlayingFromMatchSongs = true
            binding.playNextSong.isEnabled = false
            binding.playNextSong.alpha = 0.5f
        } else {
            RequestKeysAndCodes.isPlayingFromMatchSongs = false
            RequestKeysAndCodes.isPlayingFromRankSongs = true
            this.positionOfRank = position
            binding.playNextSong.isEnabled = true
            binding.playNextSong.alpha = 1.0f
        }
        updateSongUi(song)
        val mediaItem = convertToMediaItem(song)
        playSong(mediaItem)
        songState()
    }

    override fun passDataFromSearch(song: PlayedDataModel, position: Int, check: Int) {
        if (check == 2) {
            RequestKeysAndCodes.isPlayingFromSearch = true
            this.positionSearch = position
            binding.playNextSong.isEnabled = true
            binding.playNextSong.alpha = 1.0f
        }
        updateSongUiForSearchSongs(song)
        val mediaItem = convertToMediaItemforSearchsongs(song)
        playSong(mediaItem)
        songState()
    }

    private fun playSong(mediaItem: MediaBrowserCompat.MediaItem) {

//         mediaControllerPlayer = MediaControllerCompat.getMediaController(this)
        if (mediaControllerPlayer != null) {
            mediaControllerPlayer.transportControls.playFromMediaId(
                mediaItem.mediaId,
                mediaItem.description.extras
            )
        }
    }

    // calling userplatlistsongs api after getting global id (playlist id ) from userplaylist api
    private suspend fun callUserplaylistSongsApi() {
        for (item in playListSongsId) {
            var url = userPlaylistSongsUrl + item
            APIResult(object : ApiHandler {
                override fun onSuccess(response: Any) {
                    binding.apisyncalert.visibility = View.GONE
                    val playedModel = response as PlayedModel
//                    Log.e("userplay", "onSuccess:${playedModel} ")
                    for (i in playedModel.data!![0].relationships.tracks.data!!.indices) {
                        if (playedModel.data!![0].relationships.tracks.data[i].attributes.playParams != null && !playedModel.data!![0].relationships.tracks.data[i].attributes.playParams?.catalogId.isNullOrEmpty()) {

                            playedModel.data!![0].relationships.tracks.data[i].attributes.playParams?.catalogId.also {
                                if (it != null) {
                                    playedModel.data!![i].relationships.tracks.data[i].id = it
                                    URLs.songsArray.add(playedModel.data!![i].relationships.tracks.data[i])
                                    ///2nd check k baad.
                                    /// new add karna ha
                                    ///newArr.add( playedModel.data[i])
                                }
                            }
                        }
                    }

                    URLs.songsArray += playedModel.data!![0].relationships.tracks.data
                    Log.e("userplay", "onSuccess:${URLs.songsArray} ")

                }

                override fun onFailure(t: Throwable) {
                    Toast.makeText(
                        this@MainScreen,
                        "Api Syncing Failed u playlist song..${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("test", "onFailure: ${t.message}")
                }
            }, RetrofitClient(this).getRetrofitClient().getUserPlaylistsSongs(url))
        }
    }

    // calling user playlist api for getting global id/playlist id
    private suspend fun callUserPlaylistApi() {

        //UserPlaylists APi
        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                binding.apisyncalert.visibility = View.GONE
                val playedModel = response as PlayedModel
                playList = playedModel.data!!

                playListSongsId = ArrayList()
//                playListSongsId.add("")
                for (item in playList) {

                    item.attributes.playParams?.globalId?.let { playListSongsId.add(it) }

                }


                GlobalScope.launch {

                    callUserplaylistSongsApi()
                }

//                Log.e("libraryid", "onSuccess: ${playList}" )

            }

            override fun onFailure(t: Throwable) {
                Toast.makeText(
                    this@MainScreen,
                    "Api Syncing Failed u playlist ..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("test", "onFailure: ${t.message}")
            }
        }, RetrofitClient(this).getRetrofitClient().getUserPlaylists(userPlaylistUrl))
    }

    private suspend fun callHeavyRotateApi() {

        //Heavy Rotation APi
        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                binding.apisyncalert.visibility = View.GONE
                val playedModel = response as PlayedModel

                for (i in playedModel.data!!.indices) {
                    if (playedModel.data!![i].attributes.playParams != null && !playedModel.data!![i].attributes.playParams?.catalogId.isNullOrEmpty()) {

                        playedModel.data!![i].attributes.playParams?.catalogId.also {
                            if (it != null) {
                                playedModel.data!![i].id = it
                                URLs.songsArray.add(playedModel.data!![i])
                                ///2nd check k baad.
                                /// new add karna ha
                                ///newArr.add( playedModel.data[i])
                            }
                        }
                    }
                }

                URLs.songsArray += playedModel.data!!

                Log.e("heavyid", "onSuccess: ${URLs.songsArray}")


            }

            override fun onFailure(t: Throwable) {
                Toast.makeText(
                    this@MainScreen,
                    "Api Syncing Failed heavy..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("test", "onFailure: ${t.message}")

            }
        }, RetrofitClient(this).getRetrofitClient().getHeavyRotationSongs(heavyRotateUrl))
    }

    private suspend fun callRecentPlayApi() {

        //Recently played songs API
        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                binding.apisyncalert.visibility = View.GONE
                val playedModel = response as PlayedModel
//                Log.e("RECENT", "onSuccess: ${playedModel}")

                for (i in playedModel.data!!.indices) {
                    if (playedModel.data!![i].attributes.playParams != null && !playedModel.data!![i].attributes.playParams?.catalogId.isNullOrEmpty()) {

                        playedModel.data!![i].attributes.playParams?.catalogId.also {
                            if (it != null) {
                                playedModel.data!![i].id = it
                                URLs.songsArray.add(playedModel.data!![i])
                                ///2nd check k baad.
                                /// new add karna ha
                                ///newArr.add( playedModel.data[i])
                            }
                        }
                    }
                }

                //recursive call for getting data acc to offset
                if (!playedModel?.next.isNullOrEmpty()) {
                    recentPlayUrl = playedModel.next!!

                    GlobalScope.launch {

                        callRecentPlayApi()
                    }
                }

                URLs.songsArray += playedModel.data!!
                if (!isCall) {
                    this@MainScreen.loadFragment()
                    loading.isDismiss()
//                    binding.lottieLoader.visibility = View.GONE

                }
                isCall = true


                Log.e("songsarray11", "onSuccess:${URLs.songsArray} ")
//                recentList = playedModel.data
//
            }

            override fun onFailure(t: Throwable) {

                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    this@MainScreen,
                    "Api Syncing Failed recent..${t.message}",
                    Toast.LENGTH_LONG
                ).show()

                GlobalScope.launch {

                    callRecentPlayApi()
                }
            }

        }, RetrofitClient(this).getRetrofitClient().getRecentlySongs(recentPlayUrl))
    }

    private suspend fun callUserLibraryApi() {

        //User Library API
        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                binding.apisyncalert.visibility = View.GONE
                val playedModel = response as PlayedModel

//                Log.e("res", "onSuccess:${playedModel.data} ")

                for (i in playedModel.data!!.indices) {
                    if (playedModel.data!![i].attributes.playParams != null && !playedModel.data!![i].attributes.playParams?.catalogId.isNullOrEmpty()) {

                        playedModel.data!![i].attributes.playParams?.catalogId.also {
                            if (it != null) {
                                playedModel.data!![i].id = it
                                URLs.songsArray.add(playedModel.data!![i])
                                ///2nd check k baad.
                                /// new add karna ha
                                ///newArr.add( playedModel.data[i])
                            }
                        }
                    }
//                    else {
//                        //new added nhi karna
//                        if(playedModel.data[i].id.contains(".")) {
//                            val id = playedModel.data[i].id.replace(".","1")
//                            if (isAlphanumeric(id)) {
//                                Log.e("FILTER", "before filter ${playedModel.data.size} ")
//
//                                playedModel.data.remove(playedModel.data[i])
//                                Log.e("FILTER", "after filter ${playedModel.data.size} ")
//
//                            }
//                        }
//                    }


                }


//                libraryList = playedModel.data
                //recursive call for getting data acc to offset
                if (!playedModel.next.isNullOrEmpty()) {
                    userLibraryUrl = playedModel.next

                    GlobalScope.launch {
                        callUserLibraryApi()
                    }
                }

                URLs.songsArray += playedModel.data!!

                Log.e("userlibrary", "onSuccess:${URLs.songsArray} ")
            }

            override fun onFailure(t: Throwable) {
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    this@MainScreen,
                    "Api Syncing Failed user lib..${t.message}",
                    Toast.LENGTH_LONG
                ).show()

            }
        }, RetrofitClient(this).getRetrofitClient().getLibrarySongs(userLibraryUrl))
    }

    private fun getdeveloperToken() {

        var dev_token:String? = null
//        val user = User()
//        val checked = TokenPost(
//            u_token!!,
//        )


//        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {

                response as RefreshTokenModel
                dev_token = response.data.token

                AppPreferences.getInstance(this@MainScreen).setDeveloperToken(dev_token)
                Toast.makeText(
                    this@MainScreen,
                    "new developer token fetched",
                    Toast.LENGTH_LONG
                ).show()


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    this@MainScreen,
                    "Api Syncing Failed developer token ..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, RetrofitClientUser(this).getRetrofitClientUser(false).getDeveloperToken())
    }


    private fun refreshTokenApi() {

        val user = User()
        val checked = TokenPost(
            u_token!!,
        )


        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {

                response as RefreshTokenModel
                user.token = response.data.token
                insertData(user)

                Toast.makeText(
                    this@MainScreen,
                    "new token saved previous expired",
                    Toast.LENGTH_LONG
                ).show()


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    this@MainScreen,
                    "Api Syncing Failed refresh token ..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, RetrofitClientUser(this).getRetrofitClientUser(false).refreshToken(gson))
    }



    fun callUserProfileDataApi() {


        val checked = UserProfilePost(u_id!!)

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject


        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {

                response as UserProfileModel
                URLs.userProfileData = response.data
                URLs.noOfFollow = response.data.numberofFollowers
                URLs.noOfFollowing = response.data.numberOfFollowings

            }

            override fun onFailure(t: Throwable) {


                Toast.makeText(
                    this@MainScreen, "Api Syncing Failed User Profile Data", Toast.LENGTH_SHORT
                ).show()

                Log.e("test", "onFailure: ${t.message}")

            }
        }, RetrofitClientUser(this).getRetrofitClientUser(false).userProfileResponse(gson))
    }

    fun insertData(user: User) {

        val th = Thread(kotlinx.coroutines.Runnable {
            val isInserted = database.userDao().insertUser(user)

            Log.e("Data inserted", isInserted.toString())

        })
        th.start()
        th.join()
    }
    private fun logoutAlertDialog() {

        var alertDialog = AlertDialog.Builder(this@MainScreen)
        alertDialog.setTitle("Alert!")
            .setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, which ->

//                deleteData()
//                sharedPreference.clearSharedPreference()
//                val intent = Intent(this, LogIn::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                startActivity(intent)
//                finish()
                dialogInterface.dismiss()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, which ->

                dialogInterface.dismiss()
            })

        alertDialog.create().show()

    }

    private fun deleteData() {
        val th = Thread(Runnable {
            database.userDao().deleteUser()
        })
        th.start()
        th.join()
    }


    fun isAlphanumeric(id: String?): Boolean {
        val pattern: Pattern
        val password_pattern = "^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]+$"
        pattern = Pattern.compile(password_pattern)
        val matcher: Matcher = pattern.matcher(id)
        return matcher.matches()
    }

    override fun loadFragment() {
//        var fragment=MatchSongs()
//
//        var frgament2=supportFragmentManager.findFragmentByTag((fragment as MatchSongs).fragmentTag)
//        if(frgament2!=null){
//            replaceFragment(frgament2,fragment.fragmentTag)
//        }else{
//            replaceFragment(fragment,fragment.fragmentTag)
//        }
        callFrag(MatchSongs())

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaBrowserHelper.disconnect()


    }

    override fun onMediaBrowserConnected(mediaBrowser: MediaBrowserCompat) {
        this.mediaBrowser = mediaBrowser
        val mediaController = MediaControllerCompat(this, mediaBrowser.sessionToken)
        MediaControllerCompat.setMediaController(this, mediaController)

        mediaControllerCallBack = MediaControllerCallback()
        mediaControllerPlayer = MediaControllerCompat.getMediaController(activity)
        mediaControllerPlayer.registerCallback(mediaControllerCallBack)
        mediaControllerCallBack.onPlaybackStateChanged(mediaControllerPlayer.playbackState)
        mediaControllerCallBack.onMetadataChanged(mediaControllerPlayer.metadata)

//        mediaController.transportControls.playFromMediaId(mediaItem?.mediaId,mediaBrowser.extras)
        Toast.makeText(this, "1213", Toast.LENGTH_SHORT).show()
    }

    private fun songState() {

        binding.songPlay.setOnClickListener {
            val mediaController = MediaControllerCompat.getMediaController(this@MainScreen)
            if (mediaController != null) {
                when (mediaController.playbackState!!.state) {
                    PlaybackStateCompat.STATE_BUFFERING,
                    PlaybackStateCompat.STATE_CONNECTING,
                    PlaybackStateCompat.STATE_PLAYING -> {
                        binding.songPlay.setImageResource(R.drawable.ic_notification_play)
                        mediaController.transportControls.pause()
                    }
                    PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.STATE_PAUSED -> {
                        binding.songPlay.setImageResource(R.drawable.ic_notification_pause)
                        mediaController.transportControls.play()
                    }
                    PlaybackState.STATE_BUFFERING -> {

                    }

                    PlaybackState.STATE_ERROR -> {
                    }
                    PlaybackState.STATE_FAST_FORWARDING -> {
                    }
                    PlaybackState.STATE_NONE -> {
                    }

                    PlaybackState.STATE_REWINDING -> {
                    }
                    PlaybackState.STATE_SKIPPING_TO_NEXT -> {
                    }
                    PlaybackState.STATE_SKIPPING_TO_PREVIOUS -> {
                    }
                    PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM -> {
                    }

                }
            }

        }
    }

    private var positionOfRank: Int = 0
    private var positionSearch: Int = 0
    private var songList: ArrayList<SongCheckData> = ArrayList()
    private var songListSearch: ArrayList<PlayedDataModel> = ArrayList()


    fun setSongsData(songList: ArrayList<SongCheckData>) {
        RequestKeysAndCodes.songList = songList.map { convertToString(it) } as ArrayList<String>
        this.songList = songList
        //makeQueue()
    }

    fun setSongsDataForSearch(songListSearch: ArrayList<PlayedDataModel>) {
        RequestKeysAndCodes.songListSearch =
            songListSearch.map { convertToStringForSearch(it) } as ArrayList<String>
        this.songListSearch = songListSearch
        //makeQueue()
    }

    /*  private fun makeQueue() {
          val list = RequestKeysAndCodes.songList.map { convertToString(it) }
          queueProviderBuilder = CatalogPlaybackQueueItemProvider.Builder()
          queueProviderBuilder.items(MediaItemType.SONG, *list.toTypedArray())
          *//*queueProviderBuilder.startItemIndex()*//*

    }*/


    private fun convertToString(reader: SongCheckData): String? {
        return reader.songCode
    }

    private fun convertToMediaItem(reader: SongCheckData): MediaBrowserCompat.MediaItem {
        var flags = 0
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
        mediaDescriptionBuilder.setMediaId(reader.songCode)
        mediaDescriptionBuilder.setTitle(reader.songTitle)
        mediaDescriptionBuilder.setSubtitle(reader.artistTitle)
        mediaDescriptionBuilder.setDescription("")
        //flags = flags or MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        flags = flags or MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//        if (reader.image.contains("{w}x{h}bb")) {
//            picnotif = reader.image.replace("{w}x{h}bb", "100x100bb")
//        }
//        val byteArray: ByteArray = picnotif!!.toByteArray()
//        bitmap = Imgconvertors.toBitmap(byteArray)
        mediaDescriptionBuilder.setIconUri(Uri.parse(reader.image))
        return MediaBrowserCompat.MediaItem(mediaDescriptionBuilder.build(), flags)
    }

    private fun convertToStringForSearch(reader: PlayedDataModel): String {
        return reader.id
    }

    private fun convertToMediaItemforSearchsongs(reader: PlayedDataModel): MediaBrowserCompat.MediaItem {
        var flags = 0
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
        mediaDescriptionBuilder.setMediaId(reader.id)
        mediaDescriptionBuilder.setTitle(reader.attributes.name)
        mediaDescriptionBuilder.setSubtitle(reader.attributes.artistName)
        mediaDescriptionBuilder.setDescription("")
        //flags = flags or MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        flags = flags or MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//        if (reader.image.contains("{w}x{h}bb")) {
//            picnotif = reader.image.replace("{w}x{h}bb", "100x100bb")
//        }
//        val byteArray: ByteArray = picnotif!!.toByteArray()
//        bitmap = Imgconvertors.toBitmap(byteArray)
        mediaDescriptionBuilder.setIconUri(Uri.parse(reader.attributes.artwork.url))
        return MediaBrowserCompat.MediaItem(mediaDescriptionBuilder.build(), flags)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            if (state == null) {
                return
            }
            currentPlaybackState = state
            when (state.state) {
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_STOPPED -> {
                }
                else -> {
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            if (metadata != null) {
                val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                binding.playerProgress.max = duration.toInt()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainScreen@", "OnResume")
        RequestKeysAndCodes.setPassThePositionObject(this)
    }

    override fun positionOfCurrentSong(position: Int) {
        if (position >= 0 && position < songList.size) {
            this.positionOfRank = position
            updateSongUi(songList[position])
            RequestKeysAndCodes.passingToFragment(position)
//            updateSongUiForSearchSongs(songListSearch[position])
//            RequestKeysAndCodes.upDateThePositionSearch(position)
        }
    }

    override fun positionOfCurrentSongSearch(position: Int) {
        if (position >= 0 && position < songListSearch.size) {
            this.positionSearch = position
            updateSongUiForSearchSongs(songListSearch[position])
//            RequestKeysAndCodes.upDateThePositionSearch(position)
            RequestKeysAndCodes.passingToFragment(position)
        }
    }


    private fun updateSongUi(song: SongCheckData) {

        if (song.image!!.contains("{w}x{h}bb")) {
            pic = song.image!!.replace("{w}x{h}bb", "100x100bb")
        }
        runOnUiThread {
            binding.songPic.load(pic) {
                placeholder(R.drawable.ic_baseline_library_music_24)
            }
            binding.songTitle.text = song.songTitle
            binding.songArtist.text = song.artistTitle
            binding.musicplayer.visibility = View.VISIBLE
        }
    }

    private fun updateSongUiForSearchSongs(song: PlayedDataModel) {

        if (song.attributes.artwork.url.contains("{w}x{h}bb")) {
            pic = song.attributes.artwork.url.replace("{w}x{h}bb", "100x100bb")
        }
        runOnUiThread {
            binding.songPic.load(pic) {
                placeholder(R.drawable.ic_baseline_library_music_24)
            }
            binding.songTitle.text = song.attributes.name
            binding.songArtist.text = song.attributes.artistName
            binding.musicplayer.visibility = View.VISIBLE
        }
    }

    fun changeBottom(id: Int) {
        binding.bottomNavigation.selectedItemId = id
    }


}


