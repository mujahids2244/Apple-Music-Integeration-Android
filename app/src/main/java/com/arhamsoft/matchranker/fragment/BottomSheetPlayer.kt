package com.arhamsoft.matchranker.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapter
import com.arhamsoft.matchranker.databinding.FragmentBottomSheetPlayerBinding
import com.arhamsoft.matchranker.interfaces.Communicator
import com.arhamsoft.matchranker.interfaces.PassThePositionOfPlayingSong
import com.arhamsoft.matchranker.interfaces.PassThePositionOfPlayingSongFromSearch
import com.arhamsoft.matchranker.models.PlayedDataModel
import com.arhamsoft.matchranker.models.SongCheckData
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.service.MediaBrowserHelperJava
import com.arhamsoft.matchranker.util.RequestKeysAndCodes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.IOException


class BottomSheetPlayer : BottomSheetDialogFragment(), PassThePositionOfPlayingSong,PassThePositionOfPlayingSongFromSearch {

    lateinit var binding: FragmentBottomSheetPlayerBinding
    private var handler = Handler(Looper.getMainLooper())
    private var totalTime: Double = 0.0
    lateinit var runnable: Runnable
    private lateinit var mediaBrowserHelper: MediaBrowserHelperJava
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var id: String? = null
    private lateinit var songCheckData: SongCheckData
    private lateinit var mediaItem: MediaBrowserCompat.MediaItem
    private var currentPlaybackState: PlaybackStateCompat? = null
    var pic: String? = null
    private lateinit var communicator: Communicator
    private lateinit var mediaControllerCallBack: MediaControllerCallback
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var songList: ArrayList<SongCheckData>
    private lateinit var songListSearch: ArrayList<PlayedDataModel>
    private var positionOfRank: Int = 0
    private var positionSearch: Int = 0
    lateinit var recyclerView: RecyclerView
//    lateinit var loading: LoadingDialog
    private lateinit var rvAdapter: RVAdapter
    private var audioManager: AudioManager? = null


    companion object {
        lateinit var activity: AppCompatActivity
    }


    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomSheetPlayerBinding.inflate(LayoutInflater.from(context))
//        Blurry.with(requireContext()).radius(25).sampling(2).onto(binding.backgrounblur)
//        Blurry.with(requireContext()).capture(view).into(binding.backgrounblur)
        VolumeControls()
        val bottomSheetDialog = dialog as BottomSheetDialog
        val metrics = DisplayMetrics()
        requireActivity().windowManager?.defaultDisplay?.getMetrics(metrics)

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.peekHeight = metrics.heightPixels

        songList = RequestKeysAndCodes.songListPlayer
        songListSearch = RequestKeysAndCodes.songListPlayerSearch

        positionOfRank = RequestKeysAndCodes.position
        positionSearch = RequestKeysAndCodes.positionSearch
//        binding.back.setBackgroundResource(com.arhamsoft.matchranker.R.drawable.blurbackground)
//        songList = intent.getSerializableExtra(RequestKeysAndCodes.songListData) as ArrayList<SongCheckData>
//        position = intent.getIntExtra(RequestKeysAndCodes.songListDataPosition, 0)
        mediaControllerCallBack = MediaControllerCallback()
//        loading = LoadingDialog()
        mediaController = MediaControllerCompat.getMediaController(MainScreen.activity)
        mediaController.registerCallback(mediaControllerCallBack)
        mediaControllerCallBack.onPlaybackStateChanged(mediaController.playbackState)
        mediaControllerCallBack.onMetadataChanged(mediaController.metadata)
//        activity = this
        communicator = MainScreen.activity as Communicator
        recyclerView = binding.recycleList
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        rvAdapter = RVAdapter(requireContext(), songList,0, object : RVAdapter.OnItemClick {
            override fun onClick(song: SongCheckData, position: Int) {
//                Toast.makeText(requireContext(),"clicked",Toast.LENGTH_SHORT).show()
//                loading.startLoading("Please Wait")

                this@BottomSheetPlayer.positionOfRank = position
                updateUI(song)
                 communicator.passData(song, position)
//                loading.isDismiss()



//                song.let {
//                    mediaItem = readItem(it)
//                    play()
//                }
            }

            override fun onbtnClick(song: SongCheckData, position: Int) {
            }

            override fun onForDetailClick(song: SongCheckData, position: Int) {

            }

        })
        recyclerView.adapter = rvAdapter



//        recyclerView.isNestedScrollingEnabled = false


        if (RequestKeysAndCodes.isPlayingFromMatchSongs) {
            updateUI(URLs.currentSong!!)
            binding.forwardsSong.isEnabled = false
            binding.forwardsSong.alpha = 0.5f
//            songList.clear()
        }else if (RequestKeysAndCodes.isPlayingFromSearch){
            updateUISearch(songListSearch[positionSearch])
            binding.forwardsSong.isEnabled = true
            binding.forwardsSong.alpha = 1.0f
        }
        else if(RequestKeysAndCodes.isPlayingFromRankSongs) {
            updateUI(songList[positionOfRank])
            binding.forwardsSong.isEnabled = true
            binding.forwardsSong.alpha = 1.0f
        }

//        mediaBrowserHelper = MediaBrowserHelperJava(this, this)
//        id = intent.getStringExtra("songId")
//        val songObj=intent?.extras?.getString("songObj", null)
//        songObj?.let {
//            songCheckData=Gson().fromJson(it, SongCheckData::class.java)
//            songCheckData?.let {
        //mediaItem = URLs.currentSong?.let { readItem(it) }!!
//            }
//        }
//        if (mediaBrowser != null && mediaBrowser.isConnected()) {
//            Bottomsheet_Player().onMediaBrowserConnected(mediaBrowser)
//        }
//        val mediaplayer = MediaPlayer.create(this, com.arhamsoft.matchranker.R.raw.music)

        /*val mediaController = MediaControllerCompat.getMediaController(this)
        mediaController.transportControls.playFromMediaId(
            id,
            mediaItem.getDescription().getExtras()
        )*/




        binding.seekbar.progress = 0
//        binding.seekbar.max = mediaController.playbackState?.position!!.toInt()


//        binding.closeSheet.setOnClickListener {
//
//            onBackPressed()
//            overridePendingTransition(0, com.arhamsoft.matchranker.R.anim.slide_down2)
//            finish()
//        }

        binding.previousSong.setOnClickListener {
            mediaController.transportControls.skipToPrevious()
        }

        binding.forwardsSong.setOnClickListener {

            mediaController.transportControls.skipToNext()
        }


        binding.playSong.setOnClickListener {
//            binding.playSong.setImageResource(R.drawable.ic_notification_play)
            play()
            /*val mediaControllerPlayer = MediaControllerCompat.getMediaController(this)
            if (id != null)
                mediaControllerPlayer.transportControls.playFromMediaId(
                    id,
                    mediaBrowser.extras
                )*/

//
//            if(!mediaplayer.isPlaying){
//                mediaplayer.start()
//
//                binding.playSong.setImageResource(R.drawable.ic_baseline_pause_24)
//            }
//            else
//            {
//                mediaplayer.pause()
//
//                binding.playSong.setImageResource(R.drawable.play)
//
//            }

//            when the music finish seekbar will be back to 0 and play image change
//            mediaController.completeListener {
//                binding.playSong.setImageResource(R.drawable.play)
//                binding.seekbar.progress = 0
//            }


        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, position: Int, changed: Boolean) {

                //if change the position of seekbar, music will go to that position
                if (changed) {
                    mediaController.transportControls.seekTo(position.toLong())
//                    mediaplayer.seekTo(position)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })



//        binding.soundSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, changed: Boolean) {
//                if (changed) {
//                    var volumeNum = progress / 100.0f
//
//                    mediaController.playbackInfo.volumeControl
//                    mediaplayer.setVolume(volumeNum, volumeNum)
//                }
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//
//            }
//        })


//        while the song is playing, changing the position of seekbar
        runnable = Runnable {
//            val mediaController = MediaControllerCompat.getMediaController(MainScreen.activity)
//            val currentPosition = currentPlaybackState!!.position
//
//            binding.seekbar.progress = currentPosition.toInt()
//
//            //song current duration
//            var elapsedTime = createTimeLabel(currentPosition.toDouble())
//            binding.songPosition.text = elapsedTime
            updatePosition()
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)


        return binding.root

    }

//    override fun onStart() {
//        super.onStart()
//        binding.blur.startBlur()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        binding.blur.pauseBlur()
//    }

    private fun VolumeControls() {
        try {
            audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            binding.soundSeekbar.max = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            binding.soundSeekbar.progress = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            binding.soundSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(arg0: SeekBar) {}
                override fun onStartTrackingTouch(arg0: SeekBar) {}
                override fun onProgressChanged(arg0: SeekBar, progress: Int, arg2: Boolean) {
                    audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0
                    )
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createTimeLabel(time: Double): String {
        var timeLabel = ""
//        var min = time / 1000 / 60
////        var sec = time / 1000 % 60

        var min = time % 3600 / 60
        var sec = time % 3600 % 60



        timeLabel = "${String.format("%.0f", min)}:"
        if (sec < 10)
            timeLabel += "0"
        timeLabel += String.format("%.0f", sec)

        return timeLabel
    }



    private fun updatePosition() {
        if (currentPlaybackState == null) {
            return
        }
        var currentPosition = currentPlaybackState!!.position
        if (currentPlaybackState!!.state == PlaybackStateCompat.STATE_PLAYING) {
            val timeDelta =
                SystemClock.elapsedRealtime() - currentPlaybackState!!.lastPositionUpdateTime
            currentPosition += (timeDelta * currentPlaybackState!!.playbackSpeed).toLong()
        }
        binding.seekbar.progress = currentPosition.toInt()
        val elapsedTime = getTimeString(currentPosition)
        binding.songPosition.text = elapsedTime
    }


    private fun getTimeString(millis: Long): String? {
        val buf = StringBuffer()
        val hours = (millis / (1000 * 60 * 60)).toInt()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        buf
            .append(String.format("%02d", minutes))
            .append(":")
            .append(String.format("%02d", seconds))
        return buf.toString()
    }

    private fun play() {
        if (mediaController != null) {
            when (mediaController.playbackState!!.state) {
                PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_CONNECTING,
                PlaybackStateCompat.STATE_PLAYING -> {
                    binding.playSong.setImageResource(com.arhamsoft.matchranker.R.drawable.ic_notification_play)
                    mediaController.transportControls.pause()
                }
                PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.STATE_PAUSED -> {
                    binding.playSong.setImageResource(com.arhamsoft.matchranker.R.drawable.ic_notification_pause)
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

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            if (state == null) {
                return
            }
            currentPlaybackState = state
            when (state.state) {
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_STOPPED ->{}
                else -> {}
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            if (metadata != null) {
                val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                binding.seekbar.max = duration.toInt()
            }
        }
    }

    override fun positionOfCurrentSong(position: Int) {
        if (position >= 0 && position < songList.size) {
            this.positionOfRank = position
            updateUI(songList[position])
        }
    }

    private fun updateUI(songData: SongCheckData) {

        totalTime = songData.duration!!
        val total = createTimeLabel(totalTime)


        pic = songData.image
        if (pic!!.contains("{w}x{h}bb")) {
            pic = pic!!.replace("{w}x{h}bb", "500x500bb")
        }
        requireActivity().runOnUiThread {
            binding.songDuration.text = total
            binding.songTitle.text = songData.songTitle
            binding.songArtist.text = songData.artistTitle
            binding.songPic.load(pic) {
                placeholder(com.arhamsoft.matchranker.R.drawable.ic_baseline_library_music_24)
            }
        }
    }

    private fun updateUISearch(songData: PlayedDataModel) {

        totalTime = songData.attributes.durationInMillis.toDouble()
        val total = createTimeLabel(totalTime)


        pic = songData.attributes.artwork.url
        if (pic!!.contains("{w}x{h}bb")) {
            pic = pic!!.replace("{w}x{h}bb", "500x500bb")
        }
        requireActivity().runOnUiThread {
            binding.songDuration.text = total
            binding.songTitle.text = songData.attributes.name
            binding.songArtist.text = songData.attributes.artistName
            binding.songPic.load(pic) {
                placeholder(com.arhamsoft.matchranker.R.drawable.ic_baseline_library_music_24)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        RequestKeysAndCodes.setPassToFragmentInterface(this)
        RequestKeysAndCodes.setPassThePositionObjectSearch(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        RequestKeysAndCodes.setPassToFragmentInterface(null)
        RequestKeysAndCodes.setPassThePositionObjectSearch(null)
        if (RequestKeysAndCodes.isPlayingFromRankSongs) {
            RequestKeysAndCodes.upDateThePosition(positionOfRank)
        }
        else if(RequestKeysAndCodes.isPlayingFromSearch) {
            RequestKeysAndCodes.upDateThePositionSearch(positionSearch)
        }

    }

    override fun positionOfCurrentSongSearch(position: Int) {
        if (position >= 0 && position < songListSearch.size) {
            this.positionSearch = position
            updateUISearch(songListSearch[position])
        }
    }
}