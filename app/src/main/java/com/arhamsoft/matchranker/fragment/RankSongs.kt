package com.arhamsoft.matchranker.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.activity.SongDetails
import com.arhamsoft.matchranker.adapter.RVAdapter
import com.arhamsoft.matchranker.databinding.FragmentRankSongsBinding
import com.arhamsoft.matchranker.interfaces.Communicator
import com.arhamsoft.matchranker.models.*
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.arhamsoft.matchranker.util.Imgconvertors
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tablitsolutions.crm.activities.OnLoadMoreListener
import com.tablitsolutions.crm.activities.RecyclerViewLoadMoreScroll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class RankSongs : Fragment(R.layout.fragment_rank_songs), SwipeRefreshLayout.OnRefreshListener {

    lateinit var binding: FragmentRankSongsBinding
    var flag: Boolean = false
    private lateinit var communicator: Communicator
    var u_id: String? = null
    lateinit var loading: LoadingDialog
    lateinit var recyclerView: RecyclerView
    lateinit var rvLoadMore: RecyclerViewLoadMoreScroll
    private lateinit var rvAdapter: RVAdapter
    var songsList: ArrayList<SongCheckData> = ArrayList()
    var tempsongsList: ArrayList<SongCheckData> = ArrayList()
    private var positionStart: Int = 0
    private var positionEnd: Int = 0
    private var totalChange: Double = 0.0
    private var grossDiff: Double = 0.0
    var bitmap: Bitmap? = null
    var picnotif: String? = null
    var statusRejected: RejectedResponse? = null
    private var bottomFragment: SongDetailsBottomDialog? = null


    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaItem: MediaBrowserCompat.MediaItem


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRankSongsBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)
        communicator = requireActivity() as Communicator
        recyclerView = binding.recycleList
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        tempsongsList = ArrayList()

        binding.refreshList.setOnRefreshListener(this)

        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
            getSongsFromServer(0, false)
            withContext(Dispatchers.Main) {
                loading.startLoading("Please Wait")
            }
        }


        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.e("text", "beforeTextChanged: ")
            }

            override fun onTextChanged(
                newText: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    searchSongsFromServer(newText!!.toString())
                }
                binding.cancelSearch.setOnClickListener {
                    binding.search.text.clear()
                    tempsongsList.clear()
                    rvAdapter.addData(songsList)
                   /// rvAdapter.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                Log.e("text", "afterTextChanged: ")
            }
        })


        initScrollListener()
        if (songsList == null && songsList.isEmpty()) {

            binding.noData.visibility = View.VISIBLE
        } else {
            binding.noData.visibility = View.GONE
        }
        rvAdapter = RVAdapter(requireContext(), songsList, 1, object : RVAdapter.OnItemClick {
            override fun onClick(song: SongCheckData, position: Int) {
                loading.startLoading("Please Wait")
//                URLs.currentSong = song
                communicator.passData(song, position)
                loading.isDismiss()

            }

            override fun onbtnClick(song: SongCheckData, position: Int) {

            }

            override fun onForDetailClick(song: SongCheckData, position: Int) {
                URLs.currentSong = song
                bottomFragment = SongDetailsBottomDialog()
                (activity as MainScreen).showFragment(SongDetailsBottomDialog())
                URLs.fromRank = 0
//                bottomFragment!!.show( MainScreen.activity.supportFragmentManager.beginTransaction(),"frag2")

//            val intent = Intent(this, BottomSheetPlayer::class.java)
//                val intent = Intent(requireContext(), SongDetails::class.java)
//                intent.putExtra("songObj", song)
//                startActivity(intent)
            }

        })
        recyclerView.adapter = rvAdapter

        //drag and drop item of recyclerview
        val touchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
            ) {

                override fun onMove(
                    p0: RecyclerView,
                    p1: RecyclerView.ViewHolder,
                    p2: RecyclerView.ViewHolder
                ): Boolean {
                    return true
                }

                //simply swap and got drop pos
                override fun onMoved(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    fromPos: Int,
                    target: RecyclerView.ViewHolder,
                    toPos: Int,
                    x: Int,
                    y: Int
                ) {
                    super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                    positionEnd = toPos
                    if (fromPos != toPos) {
                        Collections.swap(songsList, fromPos, toPos)
                        rvAdapter.notifyItemMoved(fromPos, toPos)
                    }

                }

                //getting start pos and trading points
                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    if (actionState == 2) {
                        positionStart = viewHolder?.layoutPosition!!
                        URLs.rankCurrentSong = songsList[positionStart]

                        Toast.makeText(requireContext(), "picked", Toast.LENGTH_SHORT).show()


                    } else {
                        Toast.makeText(requireContext(), "dropped", Toast.LENGTH_SHORT).show()
                        if (positionEnd == 0) {
//                            positionEnd = positionStart
//                            val moveNewOld: Double = songsList[positionEnd].points
//                            val moveNew: SongCheckData = songsList[positionEnd]
//                            val targetSong: SongCheckData = songsList[positionEnd - 1]
//
//                            for (item in positionStart until positionEnd) {
//
//                                if (positionStart != positionEnd) {
//                                    grossDiff += (songsList[item].points - moveNewOld)
//                                    Log.e("loop", "onSelectedChanged: $grossDiff")
//                                }
//                            }
//
//                            moveNew.points = targetSong.points
//                            totalChange = (moveNew.points - moveNewOld)
//
//                            for (item in positionStart until positionEnd) {
//
//                                songsList[item].points =
//                                    songsList[item].points - ((songsList[item].points - moveNewOld) / grossDiff) * totalChange
////                            if(positionStart < positionEnd){
//////                                songsList[positionEnd-1].rowNo = positionEnd++.toLong()
////                                songsList[positionEnd].rowNo = positionStart.toLong()
//////                                songsList[positionEnd-1].rowNo = (positionEnd-2).toLong()
////                                songsList[positionStart].rowNo = positionStart++.toLong()
////                              //  songsList[positionEnd-1].rowNo = (positionEnd).toLong()
////
////                              //  songsList[positionEnd+1].rowNo = positionEnd.toLong()
////
////
////
////                            }
//
//                            }
                        } else if (positionEnd > positionStart)
                        //move rank song up to downward direction
                        {

                            val moveNewOld: Double = songsList[positionEnd].points!!
                            val moveNew: SongCheckData = songsList[positionEnd]
                            val targetSong: SongCheckData = songsList[positionEnd - 1]

                            moveNew.points = targetSong.points

                            for (item in positionStart until positionEnd) {

                                if (positionStart != positionEnd) {
                                    grossDiff += (songsList[item].points!! - moveNewOld)
                                    Log.e("loop", "onSelectedChanged: $grossDiff")
                                }
                            }

                            totalChange = (moveNew.points!! - moveNewOld)

                            for (item in positionStart until positionEnd) {

                                if(grossDiff == 0.0 && totalChange == 0.0){
                                    songsList[item].points = songsList[item].points!! - (songsList[item].points!! - moveNewOld)
                                }
                                else {
                                    songsList[item].points =
                                        songsList[item].points!! - (((songsList[item].points!! - moveNewOld) / grossDiff) * totalChange)
                                    Log.e("loop", "onSelectedChanged: $grossDiff")
                                }
                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                winLoseDecisionApi(songsList,true,false,positionStart,positionEnd)
                            }

                        } else if (positionEnd < positionStart)
                        //move rank song down to upward direction
                        {

                            val moveNewOld: Double = songsList[positionEnd].points!!
                            val moveNew: SongCheckData = songsList[positionEnd]
                            val targetSong: SongCheckData = songsList[positionEnd + 1]

                            moveNew.points = targetSong.points

                            for (item in positionStart downTo positionEnd + 1) {

                                if (positionStart != positionEnd)
                                {
                                    grossDiff += (songsList[item].points!! - moveNewOld)
                                    Log.e("loop", "onSelectedChanged: $grossDiff")
                                }

                            }

                            totalChange = (moveNew.points!! - moveNewOld)

                            for (item in positionStart downTo positionEnd + 1) {

                                if(grossDiff == 0.0 && totalChange == 0.0){
                                    songsList[item].points = songsList[item].points!! - (songsList[item].points!! - moveNewOld)

                                }
                                else
                                {
                                    songsList[item].points =
                                        songsList[item].points!! - (((songsList[item].points!! - moveNewOld) / grossDiff) * totalChange)
                                }
                                    Log.e("loop", "onSelectedChanged: $grossDiff")

                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                winLoseDecisionApi(songsList,false,true,positionStart,positionEnd)
                            }
                        }


                        rvAdapter.addData(songsList)

                       /* Handler(Looper.getMainLooper()).post(Runnable {

                          //  rvAdapter.notifyDataSetChanged()

                        })*/
                        totalChange = 0.0
                        grossDiff = 0.0
                    }
                    Log.d("sdfsfs", "$actionState")
                    super.onSelectedChanged(viewHolder, actionState)
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }


            })

        touchHelper?.attachToRecyclerView(recyclerView)


        binding.ivsearch.setOnClickListener {

            if (flag) {
                binding.searchVisibility.animate()
                    .translationY(0F)
                    .alpha(0.0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.searchVisibility.visibility = View.GONE
                        }
                    })

                flag = false
            } else {
                binding.searchVisibility.visibility = View.VISIBLE
                binding.searchVisibility.alpha = 0.0f
                binding.searchVisibility.animate()
                    .translationY(1F)
                    .alpha(1.0f)
                    .setListener(null)
                flag = true
            }

        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            (activity as MainScreen)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun winLoseDecisionApi(
        songData: ArrayList<SongCheckData>,
        listFlag: Boolean,
        songFlag: Boolean,
        startPosition: Int,
        endPosition: Int
    ) {

        val arrayList = ArrayList<RejectedSongData>()
        if(endPosition > startPosition) {
            for (index in startPosition until endPosition) {
                arrayList.add(
                    RejectedSongData(
                        songData[index].points!!,
                        songData[index].songTitle!!,
                        songData[index].position!!,
                        songData[index].artistTitle!!,
                        songData[index].duration!!,
                        songData[index].probablity!!,
                        songData[index].kFactor!!,
                        false,
                        songData[index].image!!,
                        songData[index].playCount!!,
                        songData[index].songCode!!,
                        listFlag,
                        u_id!!
                    )
                )
            }

            arrayList.add(
                RejectedSongData(
                    songData[endPosition].points!!,
                    songData[endPosition].songTitle!!,
                    songData[endPosition].position!!,
                    songData[endPosition].artistTitle!!,
                    songData[endPosition].duration!!,
                    songData[endPosition].probablity!!,
                    songData[endPosition].kFactor!!,
                    false,
                    songData[endPosition].image!!,
                    songData[endPosition].playCount!!,
                    songData[endPosition].songCode!!,
                    songFlag,
                    u_id!!
                )
            )
        }
        else if(endPosition < startPosition){
            for (index in startPosition downTo endPosition+1){
                arrayList.add(
                    RejectedSongData(
                        songData[index].points!!,
                        songData[index].songTitle!!,
                        songData[index].position!!,
                        songData[index].artistTitle!!,
                        songData[index].duration!!,
                        songData[index].probablity!!,
                        songData[index].kFactor!!,
                        false,
                        songData[index].image!!,
                        songData[index].playCount!!,
                        songData[index].songCode!!,
                        listFlag,
                        u_id!!
                    )
                )
            }

            arrayList.add(
                RejectedSongData(
                    songData[endPosition].points!!,
                    songData[endPosition].songTitle!!,
                    songData[endPosition].position!!,
                    songData[endPosition].artistTitle!!,
                    songData[endPosition].duration!!,
                    songData[endPosition].probablity!!,
                    songData[endPosition].kFactor!!,
                    false,
                    songData[endPosition].image!!,
                    songData[endPosition].playCount!!,
                    songData[endPosition].songCode!!,
                    songFlag,
                    u_id!!
                )
            )
        }

        val checked = SongWinLoseModel(
            arrayList as List<RejectedSongData>,
            true
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(
            object : ApiHandler {
                override fun onSuccess(response: Any) {

                    statusRejected = response as RejectedResponse
                    loading.isDismiss()
                    Toast.makeText(
                        context,
                        "success win lose rank api",
                        Toast.LENGTH_LONG
                    ).show()


                }

                override fun onFailure(t: Throwable) {
                    Log.e("test", "onFailure: ${t.message}")
                    Toast.makeText(
                        context,
                        "Api Syncing Failed Win lose rank song ..${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            },
            RetrofitClientUser(requireContext()).getRetrofitClientUser(false)
                .getWinLoseResponse(gson)
        )
    }


    // api cal for searching song from server
    private fun searchSongsFromServer(songTitle: String) {

        val checked = SongSearchPost(
            songTitle,
            u_id!!
        )

//        val jsonObject = JSONObject()
//        jsonObject.put("limit", checked.limit)
//        jsonObject.put("offset", checked.offset)
//        jsonObject.put("userId", checked.userId)

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                val songModel = response as RankSongCheck

                tempsongsList = ArrayList()

                for (i in 0 until songModel.data!!.size) {
//                    if (songModel.data[i].songTitle.contains(newText!!, true)) {
                    val songs = songModel.data[i]
                    tempsongsList.add(songs)
//                    }
                }
                rvAdapter.addData(tempsongsList)
                //rvAdapter.notifyDataSetChanged()

                Log.e("server", "onSuccess: ${songModel}")
            }

            override fun onFailure(t: Throwable) {
                Log.e("testgetsongsfromserver", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed search song..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).searchRankSongs(gson))
    }


    // api call for getting data from server
    private fun getSongsFromServer(off: Int, isLoadMore: Boolean) {

        val checked = RankSongpost(
            50,
            off + 1,
            u_id!!
        )

//        val jsonObject = JSONObject()
//        jsonObject.put("limit", checked.limit)
//        jsonObject.put("offset", checked.offset)
//        jsonObject.put("userId", checked.userId)

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                binding.refreshList.isRefreshing = false
                val songModel = response as RankSongCheck
                songsList.addAll(songModel.data as List<SongCheckData>)
                (activity as MainScreen).setSongsData(songsList)
                recyclerView.adapter?.notifyDataSetChanged()
                if (isLoadMore) {
                    rvLoadMore.setLoaded()
                }

                binding.progressBar.visibility = View.GONE



                Log.e("server", "onSuccess: ${songModel}")
            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("testgetsongsfromserver", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed rank song..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).getRankSongs(gson))
    }

    private fun initScrollListener() {
        rvLoadMore = RecyclerViewLoadMoreScroll(recyclerView.layoutManager as LinearLayoutManager)
        rvLoadMore.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                loadMore()
            }
        })
        recyclerView.addOnScrollListener(rvLoadMore)
    }

    private fun loadMore() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.VISIBLE
            }
            getSongsFromServer((songsList.size), true)
        }
    }

    override fun onRefresh() {
        binding.refreshList.isRefreshing = true
        CoroutineScope(Dispatchers.IO).launch {
            getSongsFromServer(songsList.size, false)
        }
        Toast.makeText(requireContext(),"hit",Toast.LENGTH_SHORT).show()
    }


//    private fun play() {
//
//        val mediaControllerPlayer = MediaControllerCompat.getMediaController(requireActivity())
//        if (mediaControllerPlayer != null) {
//            mediaControllerPlayer.transportControls.playFromMediaId(
//                mediaItem.mediaId,
//                mediaItem.description.extras
//            )
//        }
//    }


    /*@Throws(IOException::class)
    private fun readItem(reader: SongCheckData): MediaBrowserCompat.MediaItem {
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
    }*/


//    if (reader.image.contains("{w}x{h}bb")) {
//            picnotif = reader.image.replace("{w}x{h}bb", "100x100bb")
//            var inputStream: InputStream? = null
//            val thread = Thread( Runnable{
//                try {
//                    inputStream = URL(picnotif).openStream()
//                    bitmap = BitmapFactory.decodeStream(inputStream)
//                    mediaDescriptionBuilder.setIconBitmap(bitmap)
//                } catch (e: IOException) {
//                    System.err.printf(
//                        "Failed while reading bytes from %s: %s",
//                        picnotif,
//                        e.message
//                    )
//                    e.printStackTrace()
//                    // Perform any other exception handling that's appropriate.
//                } finally {
//                    inputStream?.close()
//                }
//            })
//            thread.start()
//            thread.join()
//            /*val byteArray: ByteArray = picnotif!!.toByteArray()
//            bitmap = Imgconvertors.toBitmap(byteArray)*/
//        }

}

