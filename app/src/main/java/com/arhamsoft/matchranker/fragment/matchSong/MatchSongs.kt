package com.arhamsoft.matchranker.fragment.matchSong

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.databinding.FragmentMatchSongsBinding
import android.view.View.OnTouchListener
import android.widget.Toast
import coil.load
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.interfaces.Communicator
import com.arhamsoft.matchranker.models.*
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.User
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.arhamsoft.matchranker.util.RequestKeysAndCodes
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.pow


class MatchSongs : Fragment() {


    lateinit var binding: FragmentMatchSongsBinding
    lateinit var loading: LoadingDialog

    var pic: String? = null
    var pic2: String? = null
    private var totalTime: Double = 0.0
    private var totalTime2: Double = 0.0
    private lateinit var communicator: Communicator


    private var xDelta: Float = 0.0f
    private var yDelta: Float = 0.0f
    private var xDelta2: Float = 0.0f
    private var yDelta2: Float = 0.0f
    var u_id: String? = null
    var u_token: String? = null
    private var isRandomSelected: Boolean = false
    var status: SongCheck? = null
    var status2: SongCheck? = null
    var statusRejected: RejectedResponse? = null
    private lateinit var viewModel: MatchSongsViewModel
    private lateinit var mediaItem: MediaBrowserCompat.MediaItem
    lateinit var database: UserDatabase



    val fragmentTag: String
        get() = MatchSongs::class.java.simpleName

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity as MainScreen).changeBottom(R.id.song_match)
        binding = FragmentMatchSongsBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)
        communicator = requireActivity() as Communicator
        database = UserDatabase.getDatabase(requireContext())
        Toast.makeText(requireContext(), "matchsongs", Toast.LENGTH_SHORT).show()

//        viewModel = ViewModelProvider(this)[MatchSongsViewModel::class.java]

        if (!isRandomSelected) {

            URLs.song1 = randomSong()
            URLs.song2 = randomSong()
            if (URLs.song2 == URLs.song1) {

                URLs.song2 = randomSong()
                URLs.song1 = randomSong()
            }

        }
        isRandomSelected = true

        CoroutineScope(Dispatchers.IO).launch {
            val user = database.userDao().getUser()
            u_id = user.userId
            u_token = user.token
            if (URLs.matchupCheck == 1){
                URLs.song1!!.id = URLs.matchupSongId!!
                checkSong1DetailsApi()
                checkSong2DetailsApi()
            }
            else if(URLs.matchupCheck == 0){
                checkSong1DetailsApi()
                checkSong2DetailsApi()
            }
            else if(URLs.matchupCheck == 2){
                URLs.song1 = URLs.matchupSongSearchApple
                checkSong1DetailsApi()
                checkSong2DetailsApi()
            }

            withContext(Dispatchers.Main) {
                loading.startLoading("Please Wait")

            }
        }

//        if (viewModel.songsModel1 == null) {
//
//            }
//        } else {
//            val data = viewModel.songsModel1
//            data!!.pic1?.let {
//                Picasso.get().load(it).placeholder(R.drawable.ic_baseline_library_music_24)
//                    .error(R.drawable.ic_baseline_library_music_24).into(binding.songPic)
//            }
//            binding.tvartist.text = data.title1
//            binding.tvsongName.text = data.name1
//
//        }


        binding.playSong1.setOnClickListener {

            loading.startLoading("Please Wait")
            URLs.currentSong = URLs.leftSong
            URLs.leftSong?.let { it1 -> communicator.passData(it1, -1) }
            loading.isDismiss()
//            URLs.leftSong?.let {
//                mediaItem = readItem(it)
//            }
            //play()

        }

        binding.playSong2.setOnClickListener {
            loading.startLoading("Please Wait")
            URLs.currentSong = URLs.rightSong
            URLs.rightSong?.let { it1 -> communicator.passData(it1, -1) }
            loading.isDismiss()

//            URLs.rightSong?.let {
//                mediaItem = readItem(it)
//            }
            //play()
        }



        binding.view1.setOnTouchListener { v, event ->
            Boolean
            val action = event.action
            when (action) {

                MotionEvent.ACTION_DOWN -> {

                    yDelta = event.rawY - v.y
                    xDelta = event.rawX - v.x


                }

                MotionEvent.ACTION_MOVE -> {

                    v.y = event.rawY - yDelta
                    v.x = event.rawX - xDelta


                    Log.e("v1x = ", (v.y).toString())

                    binding.view2.y = ((v.y) * (-1)) + 1080
                    binding.view2.x = (v.x) * (-1) + 720

                    Log.e("v2x = ", (binding.view2.y).toString())

                }

                MotionEvent.ACTION_UP -> {

                    if (v.y < 110.0) {

                        v.animate()
                            .y( 97.984F)
                            .x( 350.0F)
                        binding.view2.animate()
                            .y(802.016F)
                            .x(350.0F)
                        winScenario(URLs.leftSong, URLs.rightSong)
                        CoroutineScope(Dispatchers.IO).launch {
                            winLoseDecisionApi(URLs.leftSong,URLs.rightSong,true,false)
                            withContext(Dispatchers.Main){
                                loading.startLoading("Please Wait")
                            }
                        }

                        //replace with api post request
                        // win or lose scenario of song
                        Handler(Looper.getMainLooper()).postDelayed({
                            v.animate()
                                .y((810F) * (-1))

                            binding.view2.animate()
                                .y(2000F)


                        }, 3000)


                        //new position for both songs
                        Handler(Looper.getMainLooper()).postDelayed({


                            if (v.y == (810F) * (-1)) {

                                v.visibility = View.GONE
                                binding.view2.visibility = View.GONE

                                v.animate()
                                    .y(540.0F)
                                    .x((500.0F) * (-1))


                                binding.view2.animate()
                                    .x(1200.0F)
                                    .y(540.0f)

                                URLs.song1 = randomSong()
                                URLs.song2 = randomSong()
                                if (URLs.song1 == URLs.song2) {
                                    URLs.song1 = randomSong()
                                }
                                CoroutineScope(Dispatchers.IO).launch {
                                    checkSong1DetailsApi()
                                    checkSong2DetailsApi()
                                    withContext(Dispatchers.Main) {
                                        loading.startLoading("Please Wait")
                                    }
                                }

                            }

                        }, 5000)


                        //original/default  position for both songs
                        //replace with api get request
                        Handler(Looper.getMainLooper()).postDelayed({


                            if (v.x == (500.0F) * (-1) && binding.view2.x == 1200.0F) {

                                v.visibility = View.VISIBLE
                                binding.view2.visibility = View.VISIBLE

                                v.animate()
                                    .x(165.0F)
                                    .y(540.0F)

                                binding.view2.animate()
                                    .x(557.0f)
                                    .y(540.0f)

                            }
                        }, 7000)


                    } else if (v.y > 790) {


                        v.animate()
                            .y(802.016F)
                            .x(350.0F)
                        binding.view2.animate()
                            .y(97.984F)
                            .x(350.0F)
                        winScenario(URLs.rightSong, URLs.leftSong)
                        CoroutineScope(Dispatchers.IO).launch {
                            winLoseDecisionApi(URLs.rightSong,URLs.leftSong,true,false)
                            withContext(Dispatchers.Main){
                                loading.startLoading("Please Wait")
                            }
                        }


                        //replace with api post request
                        // win or lose scenario of song
                        Handler(Looper.getMainLooper()).postDelayed({
                            v.animate()
                                .y(2000F)

                            binding.view2.animate()
                                .y((810F) * (-1))

                        }, 3000)

                        //new position for both songs
                        Handler(Looper.getMainLooper()).postDelayed({

                            if (v.y == 2000F) {

                                v.visibility = View.GONE
                                binding.view2.visibility = View.GONE

                                v.animate()
                                    .y(540.0F)
                                    .x((500.0F) * (-1))


                                binding.view2.animate()
                                    .x(1200.0F)
                                    .y(540.0f)

                                URLs.song1 = randomSong()
                                URLs.song2 = randomSong()
                                if (URLs.song1 == URLs.song2) {
                                    URLs.song1 = randomSong()
                                }
                                CoroutineScope(Dispatchers.IO).launch {
                                    checkSong1DetailsApi()
                                    checkSong2DetailsApi()
                                    withContext(Dispatchers.Main) {
                                        loading.startLoading("Please Wait")
                                    }
                                }

                            }

                        }, 5000)

                        //original/default  position for both songs
                        //replace with api get request
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (v.x == (500.0F) * (-1) && binding.view2.x == 1200.0F) {

                                v.visibility = View.VISIBLE
                                binding.view2.visibility = View.VISIBLE

                                v.animate()
                                    .x(165.0F)
                                    .y(540.0F)

                                binding.view2.animate()
                                    .x(557.0f)
                                    .y(540.0f)

                            }
                        }, 7000)


                    } else if (v.x < (100.0F) * (-1)) {
                        rejectSongApi(URLs.leftSong!!, true)
                        URLs.song1 = randomSong()
                        if (URLs.song1 == URLs.song2) {
                            URLs.song1 = randomSong()
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            checkSong1DetailsApi()
                            withContext(Dispatchers.Main) {
                                loading.startLoading("Please Wait")
                            }
                        }
                        v.animate()
                            .x((500F) * (-1))


                        Handler(Looper.getMainLooper()).postDelayed({
                            if (v.x < (499F) * (-1)) {

                                v.animate()
                                    .x(165.0F)
                                    .y(540.0F)
                            }
                        }, 5000)

                        binding.view2.animate()
                            .x(557.0f)
                            .y(540.0f)

                    } else {

                        v.animate()
                            .x(165.0F)
                            .y(540.0F)

                        binding.view2.animate()
                            .x(557.0f)
                            .y(540.0f)

                    }


                }

            }
//            binding.view1.invalidate()
            true
        }

        binding.view2.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val action = event.action
                when (action) {

                    MotionEvent.ACTION_DOWN -> {
                        yDelta2 = event.rawY - v.y
                        xDelta2 = event.rawX - v.x


                    }


                    MotionEvent.ACTION_MOVE -> {
                        v.y = event.rawY - yDelta2
                        v.x = event.rawX - xDelta2

//                        v.y = event.rawY - v.height/2
//                        v.x = event.rawX - v.width/2

//                        Log.e("v2x = ", (v.x).toString())

                        Log.e("v2y = ", (v.y).toString())



                        binding.view1.y = ((v.y) * (-1)) + 1080
                        binding.view1.x = ((v.x) * (-1)) + 720

                        Log.e("v1y = ", (binding.view1.y).toString())

                    }


                    MotionEvent.ACTION_UP -> {

                        if (v.y < 110.0) {

                            v.animate()
                                .y(97.984F)
                                .x(350.0F)
                            binding.view1.animate()
                                .y(802.016F)
                                .x(350.0F)
                            winScenario(URLs.rightSong, URLs.leftSong)
                            CoroutineScope(Dispatchers.IO).launch {
                                winLoseDecisionApi(URLs.rightSong,URLs.leftSong,true,false)
                                withContext(Dispatchers.Main){
                                    loading.startLoading("Please Wait")
                                }
                            }

                            //replace with api post request
                            // win or lose scenario of song
                            Handler(Looper.getMainLooper()).postDelayed({
                                v.animate()
                                    .y((810F) * (-1))

                                binding.view1.animate()
                                    .y(2000F)

                            }, 3000)


                            //new position for both songs
                            Handler(Looper.getMainLooper()).postDelayed({

                                if (v.y == (810F) * (-1)) {

                                    v.visibility = View.GONE
                                    binding.view1.visibility = View.GONE

                                    v.animate()
                                        .x(1200.0F)
                                        .y(540.0F)

                                    binding.view1.animate()
                                        .x((500.0F) * (-1))
                                        .y(540.0f)

                                    URLs.song1 = randomSong()
                                    URLs.song2 = randomSong()
                                    if (URLs.song1 == URLs.song2) {
                                        URLs.song1 = randomSong()
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        checkSong1DetailsApi()
                                        checkSong2DetailsApi()
                                        withContext(Dispatchers.Main) {
                                            loading.startLoading("Please Wait")
                                        }
                                    }

                                }

                            }, 5000)


                            //original/default  position for both songs
                            //replace with api get request
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (v.x == 1200.0F && binding.view1.x == (500.0F) * (-1)) {

                                    v.visibility = View.VISIBLE
                                    binding.view1.visibility = View.VISIBLE

                                    v.animate()
                                        .x(557.0F)
                                        .y(540.0F)

                                    binding.view1.animate()
                                        .x(165.0f)
                                        .y(540.0f)

                                }
                            }, 7000)


                        } else if (v.y > 790) {

                            v.animate()
                                .y(802.016F)
                                .x(350.0F)
                            binding.view1.animate()
                                .y(97.984F)
                                .x(350.0F)
                            winScenario(URLs.leftSong, URLs.rightSong)
                            CoroutineScope(Dispatchers.IO).launch {
                                winLoseDecisionApi(URLs.leftSong,URLs.rightSong,true,false)
                                withContext(Dispatchers.Main){
                                    loading.startLoading("Please Wait")
                                }
                            }

                            Handler(Looper.getMainLooper()).postDelayed({
                                v.animate()
                                    .y(2000F)

                                binding.view1.animate()
                                    .y((810F) * (-1))

                            }, 3000)

                            //new position for both songs
                            Handler(Looper.getMainLooper()).postDelayed({


                                if (v.y == 2000F) {

                                    v.visibility = View.GONE
                                    binding.view1.visibility = View.GONE

                                    v.animate()
                                        .y(540.0F)
                                        .x(1200.0F)


                                    binding.view1.animate()
                                        .x((500.0F) * (-1))
                                        .y(540.0f)

                                    URLs.song1 = randomSong()
                                    URLs.song2 = randomSong()
                                    if (URLs.song1 == URLs.song2) {
                                        URLs.song1 = randomSong()
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        checkSong1DetailsApi()
                                        checkSong2DetailsApi()
                                        withContext(Dispatchers.Main) {
                                            loading.startLoading("Please Wait")
                                        }
                                    }

                                }

                            }, 5000)


                            //original/default  position for both songs
                            //replace with api get request
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (v.x == 1200.0F && binding.view1.x == (500.0F) * (-1)) {

                                    v.visibility = View.VISIBLE
                                    binding.view1.visibility = View.VISIBLE


                                    v.animate()
                                        .x(557.0F)
                                        .y(540.0F)

                                    binding.view1.animate()
                                        .x(165.0f)
                                        .y(540.0f)

                                }
                            }, 7000)

                        } else if (v.x > 800.0F) {

                            rejectSongApi(URLs.rightSong!!, true)
                            URLs.song2 = randomSong()
                            if (URLs.song2 == URLs.song1) {
                                URLs.song2 = randomSong()
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                checkSong2DetailsApi()
                                withContext(Dispatchers.Main) {
                                    loading.startLoading("Please Wait")
                                }
                            }
                            v.animate()
                                .x(1200F)

                            Handler(Looper.getMainLooper()).postDelayed({
                                if (v.x > 1199F) {

                                    v.animate()
                                        .x(557.0F)
                                        .y(540.0F)
                                }
                            }, 5000)

                            binding.view1.animate()
                                .x(165.0f)
                                .y(540.0f)


                        } else {

                            v.animate()
                                .x(557.0F)
                                .y(540.0F)

                            binding.view1.animate()
                                .x(165.0f)
                                .y(540.0f)
                        }

                    }


                }
                binding.view2.invalidate()
                return true
            }
        })
        return binding.root
    }

//    fun porbilityAsong(): Double{
//
//
//        return 1/(1+10^sta[])
//    }

    fun randomSong(): PlayedDataModel {

            return URLs.songsArray[Random().nextInt(URLs.songsArray.size)]

    }

    private fun checkSong1DetailsApi() {

        val checked = SongCheckpost(
            URLs.song1!!.id,
            u_id!!
        )

        val jsonObject = JSONObject()
        jsonObject.put("songCode", checked.songCode)
        jsonObject.put("userId", checked.userId)

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                binding.songsView.visibility = View.VISIBLE

                loading.isDismiss()
                Log.e("checksong1", "onSucces: ${response}")

                status = response as SongCheck

                if (response.isExpire){

                    refreshTokenApi()
                    CoroutineScope(Dispatchers.IO).launch {
                        checkSong1DetailsApi()
                    }
                }


                if(status!!.data == null ) {
                    status!!.data = SongCheckData()
                    status?.data?.points = 1000.00
                    status?.data?.artistTitle = URLs.song1!!.attributes.artistName
                    status?.data?.songTitle = URLs.song1!!.attributes.name
                    status?.data?.image = URLs.song1!!.attributes.artwork.url
                    status?.data?.kFactor = 40
                    status?.data?.probablity =1.0
                    status?.data?.songCode = URLs.song1!!.id
                    status?.data?.duration = URLs.song1!!.attributes.durationInMillis.toDouble()
                    status?.data?.playCount = 0
                    status?.data?.position = 1.0
                    status?.data?.isRejected = false


                    pic = status?.data?.image
                    if(URLs.song1!!.attributes.artwork.url != null){

                    if (URLs.song1!!.attributes.artwork.url.contains("{w}x{h}bb")) {
                        pic = URLs.song1!!.attributes.artwork.url.replace("{w}x{h}bb", "200x200bb")
                        binding.songPic.load(pic) {
                            placeholder(R.drawable.ic_baseline_library_music_24)
                        }
                    }}


                    val number = 1000.00
                    val rounded = String.format("%.2f", number)
                    binding.showpt.text = rounded
                    binding.tvartist.text =  URLs.song1!!.attributes.artistName
                    binding.tvsongName.text = URLs.song1!!.attributes.name
                    totalTime = URLs.song1!!.attributes.durationInMillis.toDouble()
                    val numberDur = createTimeLabel(totalTime)
                    binding.duration.text = numberDur

                    URLs.leftSong = status?.data

                }
                else if(status?.data?.isRejected!!) {
                    URLs.song1 = randomSong()
                    CoroutineScope(Dispatchers.IO).launch {
                        checkSong1DetailsApi()
                        withContext(Dispatchers.Main){
                            loading.startLoading("Please Wait")
                        }

                    }
                } else {
                    if (status?.data != null) {
                        URLs.leftSong = status?.data

//                    pic?.let {
//                        Picasso.get().load(it).placeholder(R.drawable.ic_baseline_library_music_24)
//                            .error(R.drawable.ic_baseline_library_music_24).into(binding.songPic)
//                    }

                        pic = status?.data?.image
                        if (pic!!.contains("{w}x{h}bb")) {
                            pic = pic!!.replace("{w}x{h}bb", "200x200bb")
                            binding.songPic.load(pic) {
                                placeholder(R.drawable.ic_baseline_library_music_24)
                            }
                        }


                        val number = status?.data?.points
                        val rounded = String.format("%.2f", number)
                        binding.showpt.text = rounded
                        binding.tvartist.text = status?.data?.artistTitle
                        binding.tvsongName.text = status?.data?.songTitle
                        totalTime = status?.data?.duration!!
                        val numberDur = createTimeLabel(totalTime)
//                    val roundedDur = String.format("%02d", numberDur)
                        binding.duration.text = numberDur
//                    probabilityOfA()

//                    viewModel.setData1(songsModel = SongsModel(pic!!,
//                        title1 = URLs.song1?.attributes?.artistName!!,
//                    name1 = URLs.song1?.attributes?.name!!))
                    }
                }


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed check song..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
//                CoroutineScope(Dispatchers.IO).launch {
//                    checkSong1DetailsApi()
//                }
            }

        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).checkSongDetails(gson))
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
                    context,
                    "new token saved previous expired",
                    Toast.LENGTH_LONG
                ).show()


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed refresh token ..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).refreshToken(gson))
    }


//    private fun createTimeLabel(duration: Double): String? {
//        val minutes: Long = TimeUnit.MINUTES.convert(duration.toLong(), TimeUnit.MILLISECONDS)
//        val seconds: Long = (TimeUnit.SECONDS.convert(duration.toLong(), TimeUnit.MILLISECONDS)
//                - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
//        return String.format("%02d:%02d", minutes, seconds)
//    }

    //here using this for inserting new token of user into db
    fun insertData(user: User) {

        val th = Thread(Runnable {
            val isInserted = database.userDao().insertUser(user)

            Log.e("Data inserted", isInserted.toString())

        })
        th.start()
        th.join()
    }


    fun createTimeLabel(time: Double): String {
        var timeLabel = ""
//        var min = time / 1000 / 60
////        var sec = time / 1000 % 60

        val min = time % 3600 / 60
        val sec = time % 3600 % 60


        timeLabel = "${String.format("%.0f", min)}:"
        if (sec < 10)
            timeLabel += "0"
        timeLabel += String.format("%.0f", sec)

        return timeLabel
    }

    private fun checkSong2DetailsApi() {

        val checked = SongCheckpost(
            URLs.song2!!.id,
            u_id!!
        )


        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                binding.songsView.visibility = View.VISIBLE

                Log.e("checksong2", "onSucces: ${response}")
                loading.isDismiss()
                status2 = response as SongCheck

                if (response.isExpire){

                    refreshTokenApi()
                    CoroutineScope(Dispatchers.IO).launch {
                        checkSong2DetailsApi()
                    }
                }



                if (status2?.data == null) {
                    status2!!.data = SongCheckData()

                    status2?.data?.points = 1000.00
                    status2?.data?.artistTitle = URLs.song2!!.attributes.artistName
                    status2?.data?.songTitle = URLs.song2!!.attributes.name
                    status2?.data?.image = URLs.song2!!.attributes.artwork.url
                    status2?.data?.kFactor = 40
                    status2?.data?.probablity =1.0
                    status2?.data?.songCode = URLs.song2!!.id
                    status2?.data?.duration = URLs.song2!!.attributes.durationInMillis.toDouble()
                    status2?.data?.playCount = 0
                    status2?.data?.position = 1.0
                    status2?.data?.isRejected = false



                    pic2 = status2?.data?.image
                    if(URLs.song2!!.attributes.artwork.url != null) {
                        if (URLs.song2!!.attributes.artwork.url.contains("{w}x{h}bb")) {
                            pic2 = URLs.song2!!.attributes.artwork.url.replace("{w}x{h}bb", "200x200bb")
                            binding.songPic2.load(pic2) {
                                placeholder(R.drawable.ic_baseline_library_music_24)
                            }
                        }
                    }

                    val number = 1000.00
                    val rounded = String.format("%.2f", number)
                    binding.showpt2.text = rounded
                    binding.tvartist2.text = URLs.song2!!.attributes.artistName
                    binding.tvsongName2.text = URLs.song2!!.attributes.name
                    totalTime2 = URLs.song2!!.attributes.durationInMillis.toDouble()
                    val numberDur = createTimeLabel(totalTime2)
                    binding.duration2.text = numberDur

                    URLs.rightSong = status2?.data

                }
                else if(status2?.data?.isRejected!!) {
                    URLs.song2 = randomSong()

                    CoroutineScope(Dispatchers.IO).launch {
                        checkSong2DetailsApi()
                        withContext(Dispatchers.Main){
                            loading.startLoading("Please Wait")
                        }

                    }
                } else {
                    if (status2?.data != null) {
                        URLs.rightSong = status2?.data

//                    pic2?.let {
//                        Picasso.get().load(it).placeholder(R.drawable.ic_baseline_library_music_24)
//                            .error(R.drawable.ic_baseline_library_music_24).into(binding.songPic2)
//                    }


                        pic2 = status2?.data?.image
                        if (pic2!!.contains("{w}x{h}bb")) {
                            pic2 = pic2!!.replace("{w}x{h}bb", "200x200bb")
                            binding.songPic2.load(pic2) {
                                placeholder(R.drawable.ic_baseline_library_music_24)
                            }
                        }

                        val number = status2?.data?.points
                        val rounded = String.format("%.2f", number)
                        binding.showpt2.text = rounded
                        binding.tvartist2.text = status2?.data?.artistTitle
                        binding.tvsongName2.text = status2?.data?.songTitle
                        totalTime2 = status2?.data?.duration!!
                        val numberDur = createTimeLabel(totalTime2)
//                    val roundedDur = String.format("%02d", numberDur)
                        binding.duration2.text = numberDur
//                    probabilityOfA()

                    }
                }


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed check song..${t.message}",
                    Toast.LENGTH_LONG
                ).show()

//                CoroutineScope(Dispatchers.IO).launch {
//                    checkSong2DetailsApi()
//                    }
            }

        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).checkSongDetails(gson))
    }

    private fun winLoseDecisionApi(songDataWin: SongCheckData?,songDataLose: SongCheckData?,checkWin: Boolean,checkLose: Boolean) {

        val checked = SongWinLoseModel(
            listOf(
                songDataWin?.let {
                    RejectedSongData(
                        it.points!!,
                        it.songTitle!!,
                        it.position!!,
                        it.artistTitle!!,
                        it.duration!!,
                        it.probablity!!,
                        it.kFactor!!,
                        false,
                        it.image!!,
                        it.playCount!!,
                        it.songCode!!,
                        checkWin,
                        u_id!!)
                }
            , songDataLose?.let {
                    RejectedSongData(
                        it.points!!,
                        it.songTitle!!,
                        it.position!!,
                        it.artistTitle!!,
                        it.duration!!,
                        it.probablity!!,
                        it.kFactor!!,
                        false,
                        it.image!!,
                        it.playCount!!,
                        it.songCode!!,
                        checkLose,
                        u_id!!
                    )
                }
            ),
            true
        )


//        val jsonObject = JSONObject()
//        jsonObject.put("points", checked.data[0].points)
//        jsonObject.put("songTitle", checked.data[0].songTitle)
//        jsonObject.put("position", checked.data[0].position)
//        jsonObject.put("artistTitle", checked.data[0].artistTitle)
//        jsonObject.put("duration", checked.data[0].duration)
//        jsonObject.put("probability", checked.data[0].probability)
//        jsonObject.put("kFactor", checked.data[0].kFactor)
//        jsonObject.put("songCode", checked.data[0].isRejected)
//        jsonObject.put("songCode", checked.data[0].image)
//        jsonObject.put("songCode", checked.data[0].playCount)
//        jsonObject.put("songCode", checked.data[0].songCode)
//        jsonObject.put("userId", checked.data[0].userId)

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {



                statusRejected = response as RejectedResponse
                loading.isDismiss()

                Toast.makeText(
                    context,
                    "WinLose Api",
                    Toast.LENGTH_LONG
                ).show()


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed Win lose song ..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        },
            RetrofitClientUser(requireContext()).getRetrofitClientUser(false).getWinLoseResponse(gson)
        )
    }


    private fun rejectSongApi(songData: SongCheckData, check: Boolean) {

        val checked = RejectedSongs(
            listOf(
                RejectedSongData(
                    songData.points!!,
                    songData.songTitle!!,
                    songData.position!!,
                    songData.artistTitle!!,
                    songData.duration!!,
                    songData.probablity!!,
                    songData.kFactor!!,
                    check,
                    songData.image!!,
                    songData.playCount!!,
                    songData.songCode!!,
                    false,
                    u_id!!
                )
            )
        )


        val jsonObject = JSONObject()
        jsonObject.put("points", checked.data[0].points)
        jsonObject.put("songTitle", checked.data[0].songTitle)
        jsonObject.put("position", checked.data[0].position)
        jsonObject.put("artistTitle", checked.data[0].artistTitle)
        jsonObject.put("duration", checked.data[0].duration)
        jsonObject.put("probability", checked.data[0].probability)
        jsonObject.put("kFactor", checked.data[0].kFactor)
        jsonObject.put("songCode", checked.data[0].isRejected)
        jsonObject.put("songCode", checked.data[0].image)
        jsonObject.put("songCode", checked.data[0].playCount)
        jsonObject.put("songCode", checked.data[0].songCode)
        jsonObject.put("userId", checked.data[0].userId)

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {

                statusRejected = response as RejectedResponse
                Toast.makeText(
                    context,
                    "Rejected",
                    Toast.LENGTH_LONG
                ).show()


            }

            override fun onFailure(t: Throwable) {
                Log.e("test", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed rejected song..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        },
            RetrofitClientUser(requireContext()).getRetrofitClientUser(false)
                .getRejectedResponse(gson)
        )
    }

    private fun winScenario(win: SongCheckData?, lose: SongCheckData?) {
        var probB: Double = 0.0
        val probA: Double
        if (win?.points != null && lose?.points != null) {
            val base = 10
//            val baseTenExponentB: Double = (base.toDouble().pow(win.points - lose.points)) / 400
//            probB= 1 / (1 + baseTenExponentB)
            val baseTenExponentA: Double = base.toDouble().pow((win.points!! - lose.points!!) / 400)
            probA = 1 / (1 + baseTenExponentA)

            win.probablity = probA
            lose.probablity = probA

            win.points = win.points!! + ((1 - probA) * win.kFactor!!)
            lose.points = lose.points!! - ((1 - probA) * lose.kFactor!!)

            win.kFactor  = max((win.kFactor!! - 2), 10)
            lose.kFactor = max((lose.kFactor!! - 2), 10)

            val randomWin = (1..50).random()
            val randomlose = (51..100).random()

            if (win.position!!.equals(1)) {

                win.position = (randomWin * 0.1)
            }

            if (lose.position!!.equals(1)) {

                lose.position = (randomlose * 0.1)
            }

            URLs.winName = win.songTitle
            URLs.loseName = lose.songTitle


        }
    }

    private fun play() {
        RequestKeysAndCodes.isPlayingFromMatchSongs = true
        val mediaControllerPlayer = MediaControllerCompat.getMediaController(requireActivity())
        if (mediaControllerPlayer != null) {
            mediaControllerPlayer.transportControls.playFromMediaId(
                mediaItem.mediaId,
                mediaItem.description.extras
            )
        }
    }


    @Throws(IOException::class)
    private fun readItem(reader: SongCheckData): MediaBrowserCompat.MediaItem {
        var flags = 0
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
        mediaDescriptionBuilder.setMediaId(reader.songCode)
        mediaDescriptionBuilder.setTitle(reader.songTitle)
        mediaDescriptionBuilder.setSubtitle(reader.artistTitle)
        mediaDescriptionBuilder.setDescription("")
        //flags = flags or MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        flags = flags or MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        mediaDescriptionBuilder.setIconUri(Uri.parse(reader.image))
        return MediaBrowserCompat.MediaItem(mediaDescriptionBuilder.build(), flags)
    }

}