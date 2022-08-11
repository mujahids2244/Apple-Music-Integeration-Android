package com.arhamsoft.matchranker.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.adapter.RVAdapter
import com.arhamsoft.matchranker.adapter.RVAdapterSongDetail
import com.arhamsoft.matchranker.models.*
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.arhamsoft.matchranker.util.RequestKeysAndCodes
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tablitsolutions.crm.activities.RecyclerViewLoadMoreScroll
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList

class SongDetails : AppCompatActivity() {
//    lateinit var binding: ActivitySongDetailsBinding
    lateinit var song: SongCheckData
    var u_id: String? = null
    lateinit var loading: LoadingDialog
    var songsList: ArrayList<SongDetail> = ArrayList()
    lateinit var recyclerView: RecyclerView
    private lateinit var rvAdapter: RVAdapterSongDetail
//    private lateinit var mediaControllerCallBack: MediaControllerCallback
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var mediaItem: MediaBrowserCompat.MediaItem


    var pic: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
////        binding = ActivitySongDetailsBinding.inflate(layoutInflater)
////        setContentView(binding.root)
//        loading = LoadingDialog(this)
//
//        mediaControllerCallBack = MediaControllerCallback()
//        mediaController = MediaControllerCompat.getMediaController(MainScreen.activity)
//        mediaController.registerCallback(mediaControllerCallBack)
//        mediaControllerCallBack.onPlaybackStateChanged(mediaController.playbackState)
//        mediaControllerCallBack.onMetadataChanged(mediaController.metadata)
//
//        song = intent.getSerializableExtra("songObj") as SongCheckData
//
//        pic = song!!.image
//        if (pic!!.contains("{w}x{h}bb")) {
//            pic = pic!!.replace("{w}x{h}bb", "100x100bb")
//        }
//
//        binding.songPic.load(pic) {
//            placeholder(R.drawable.ic_baseline_library_music_24)
//        }
//        binding.songTitle.text = song!!.songTitle
//        binding.songArtist.text = song!!.artistTitle
//
//        binding.songPoints.text = song!!.points.toString()
//        binding.songRanking.text = song!!.rowNo.toString()
//
//        recyclerView = binding.recycleList
//        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val user = UserDatabase.getDatabase(this@SongDetails).userDao().getUser()
//            u_id = user.userId
//            apiCallingForSongDetails()
//            withContext(Dispatchers.Main) {
//                loading.startLoading("Please Wait")
//            }
//        }
//
//        rvAdapter = RVAdapterSongDetail(this, songsList, object : RVAdapterSongDetail.OnItemClick {
//            override fun onClick(song: SongDetail, position: Int) {
//
//            }
//        })
//        recyclerView.adapter = rvAdapter
//
//        initLineChart()
//
//        binding.expandGraph.setOnClickListener {
//
//            if (binding.expandedGraph.visibility == View.GONE) {
//
//                binding.expandedGraph.visibility = View.VISIBLE
//                binding.graph.visibility = View.GONE
//            } else {
//                binding.expandedGraph.visibility = View.GONE
//                binding.graph.visibility = View.VISIBLE
//
//            }
//
//
//        }
//
//
//
//
//        binding.backtorank.setOnClickListener {
//            onBackPressed()
//        }
//
//        binding.songPlay.setOnClickListener {
//
//            RequestKeysAndCodes.isPlayingFromMatchSongs = true
//            song.let {
//                mediaItem = readItem(it)
//                playSong()
//            }
//            binding.songPlay.setImageResource(R.drawable.ic_notification_pause)
//        }
//

    }

//    override fun onBackPressed() {
//        this.finish()
//    }
//
//    private fun playSong() {
////        RequestKeysAndCodes.isPlayingFromMatchSongs = true
//
//        if (mediaController != null) {
//            mediaController.transportControls.playFromMediaId(
//                mediaItem.mediaId,
//                mediaItem.description.extras
//            )
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun readItem(reader: SongCheckData): MediaBrowserCompat.MediaItem {
//        var flags = 0
//        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
//        mediaDescriptionBuilder.setMediaId(reader.songCode)
//        mediaDescriptionBuilder.setTitle(reader.songTitle)
//        mediaDescriptionBuilder.setSubtitle(reader.artistTitle)
//        mediaDescriptionBuilder.setDescription("")
//        //flags = flags or MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
//        flags = flags or MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//        mediaDescriptionBuilder.setIconUri(Uri.parse(reader.image))
//        return MediaBrowserCompat.MediaItem(mediaDescriptionBuilder.build(), flags)
//    }
//
//    private fun initLineChart() {
//
////        hide grid lines
//        binding.graph.axisLeft.setDrawGridLines(false)
//        binding.bigGraph.axisLeft.setDrawGridLines(false)
//        val xAxis: XAxis = binding.graph.xAxis
//        xAxis.setDrawGridLines(false)
//        xAxis.setDrawAxisLine(false)
//
//        val x2Axis: XAxis = binding.bigGraph.xAxis
//        xAxis.setDrawGridLines(false)
//        xAxis.setDrawAxisLine(false)
//
//        //remove right y-axis
//        binding.graph.axisRight.isEnabled = false
//        binding.bigGraph.axisRight.isEnabled = false
//
//        //remove legend
//        binding.graph.legend.isEnabled = false
//        binding.bigGraph.legend.isEnabled = false
//
//
//        //remove description label
//        binding.graph.description.isEnabled = false
//        binding.bigGraph.description.isEnabled = false
//
//
//        //add animation
//        binding.graph.animateX(1000, Easing.EaseInSine)
//        binding.bigGraph.animateX(1000, Easing.EaseInSine)
//
//
//        // to draw label on xAxis
////        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
////        xAxis.valueFormatter = MyAxisFormatter()
////        xAxis.setDrawLabels(true)
////        xAxis.granularity = 1f
////        xAxis.labelRotationAngle = +90f
//
//    }
//
//    fun setLineChartData() {
//
//
//        val lineEntry = ArrayList<Entry>()
//        for (item in songsList.indices) {
//            lineEntry.add(Entry(item.toFloat(), songsList[item].points.toFloat()))
//        }
//
//
//        val lineDataSet = LineDataSet(lineEntry, "")
//        lineDataSet.color = resources.getColor(R.color.orange)
//        lineDataSet.valueTextColor = Color.WHITE
//        val data = LineData(lineDataSet)
//
//        binding.graph.data = data
//        binding.bigGraph.data = data
//
//    }
//
//
//    private fun apiCallingForSongDetails() {
//
//        val checked = SongCheckpost(
//            song!!.songCode,
//            u_id!!
//        )
//
//
//        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject
//
//        APIResult(object : ApiHandler {
//            override fun onSuccess(response: Any) {
//                loading.isDismiss()
//                response as SongDetailModel
//
//
//
//
//                songsList.addAll(response.data as List<SongDetail>)
//                setLineChartData()
//
//
//            }
//
//            override fun onFailure(t: Throwable) {
//                Log.e("test", "onFailure: ${t.message}")
//                Toast.makeText(
//                    this@SongDetails,
//                    "Api Syncing Failed songDetails ..${t.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//
//        }, RetrofitClientUser(this).getRetrofitClientUser(false).getSongDetailResponse(gson))
//    }
//
//    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
//        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
//            if (state == null) {
//                return
//            }
////            currentPlaybackState = state
//            when (state.state) {
//                PlaybackStateCompat.STATE_PAUSED,
//                PlaybackStateCompat.STATE_STOPPED -> {
//                }
//                else -> {}
//            }
//        }
//
//        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
//            if (metadata != null) {
//                val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
////                binding.seekbar.max = duration.toInt()
//            }
//        }
//    }


}