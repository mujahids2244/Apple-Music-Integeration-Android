package com.arhamsoft.matchranker.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapterComment
import com.arhamsoft.matchranker.adapter.RVAdapterSongDetail
import com.arhamsoft.matchranker.databinding.FragmentSongDetailsBottomDialogBinding
import com.arhamsoft.matchranker.fragment.matchSong.MatchSongs
import com.arhamsoft.matchranker.interfaces.Communicator
import com.arhamsoft.matchranker.interfaces.followFollowing.CallMethodOfFragFollow
import com.arhamsoft.matchranker.models.*
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


class SongDetailsBottomDialog : BottomSheetDialogFragment(), CallMethodOfFragFollow {

    lateinit var binding: FragmentSongDetailsBottomDialogBinding
    lateinit var song: SongCheckData
    var u_id: String? = null
    lateinit var loading: LoadingDialog
    var songsList: ArrayList<SongDetail> = ArrayList()
    var commentList: ArrayList<GetCommentDataList> = ArrayList()
    lateinit var recyclerView: RecyclerView
    private lateinit var rvAdapter: RVAdapterSongDetail
    lateinit var recyclerViewComment: RecyclerView
    private lateinit var rvAdapterComment: RVAdapterComment
    lateinit var communicator: Communicator
    var status: SongDetailModel? = null
    val set: Set<SongDetail> = HashSet()
    var winCounter: Int = 0
    var loseCounter: Int = 0
    val parentList: ArrayList<GetCommentDataList> = ArrayList()
    val childList: ArrayList<GetCommentDataList> = ArrayList()
    var commentParentId: Long = 0L
//    var emptySongList: ArrayList<SongCheckData> = ArrayList()


    var pic: String? = null


    companion object {
        lateinit var activity: AppCompatActivity
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSongDetailsBottomDialogBinding.inflate(LayoutInflater.from(context))
//        Blurry.with(requireContext()).radius(25).sampling(2).onto(binding.backgrounblur)
//        Blurry.with(requireContext()).capture(view).into(binding.backgrounblur)

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        if (URLs.fromRank == 0){
            binding.expandGraph.visibility = View.VISIBLE
        }
        else if(URLs.fromRank == 1){
            binding.expandGraph.visibility = View.GONE
            binding.viewLine.visibility = View.GONE
        }

//        binding.root.setOnApplyWindowInsetsListener { v, insets ->
//            val imeHeight = insets.getInsets(WindowInsets.Type.ime()).bottom
//            binding.root.setPadding(0, 0, 0, imeHeight)
//            insets
//        }

        communicator = requireActivity() as Communicator

        val bottomSheetDialog = dialog as BottomSheetDialog
        val metrics = DisplayMetrics()
        requireActivity().windowManager?.defaultDisplay?.getMetrics(metrics)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.peekHeight = metrics.heightPixels
        bottomSheetDialog.behavior.maxWidth = metrics.widthPixels
        bottomSheetDialog.behavior.isDraggable = false
//        bottomSheetDialog.behavior.isFitToContents = true

//        val behavior = BottomSheetBehavior.from(requireView().parent as View)
//        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        loading = LoadingDialog(requireContext() as Activity)


//        song = intent.getSerializableExtra("songObj") as SongCheckData

        song = URLs.currentSong!!

        pic = song.image
        if (pic!!.contains("{w}x{h}bb")) {
            pic = pic!!.replace("{w}x{h}bb", "100x100bb")
        }

        binding.songPic.load(pic) {
            placeholder(R.drawable.ic_baseline_library_music_24)
        }
        binding.songTitle.text = song.songTitle
        binding.songArtist.text = song.artistTitle

        val rounded = String.format("%.2f", song.points)
        binding.songPoints.text = rounded
        binding.songRanking.text = song.rowNo.toString()



        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
            apiCallingForSongDetails()
            apiCallingForSongComments()
            withContext(Dispatchers.Main) {
                loading.startLoading("Please Wait")
            }
        }



        adapterSet()

//setup Graph for songs
        initLineChart()



        binding.expandGraph.setOnClickListener {

            if (binding.expandedGraph.visibility == View.GONE) {

                binding.expandedGraph.visibility = View.VISIBLE

                binding.graph.visibility = View.GONE


            } else {
                binding.expandedGraph.visibility = View.GONE
                binding.graph.visibility = View.VISIBLE


            }
        }

        binding.postComment.setOnClickListener {

            if (binding.comment.text.isEmpty()) {
                Toast.makeText(requireContext(), "write a comment.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    if (commentParentId != 0L) {
                        postApiCallingForComments(binding.comment.text.toString(), commentParentId)
                    } else {
                        postApiCallingForComments(binding.comment.text.toString(), 0)

                    }
                    commentParentId = 0L
                    withContext(Dispatchers.Main) {
                        loading.startLoading("Plaease Wait")
                    }
                }
            }
        }



        binding.backtorank.setOnClickListener {
            this.dismiss()
        }

        binding.songPlay.setOnClickListener {

            communicator.passData(song, -1)
//            song.let {
//                mediaItem = readItem(it)
//                playSong()
//            }


//            (activity as MainScreen).setSongsData(emptySongList)

            binding.songPlay.setImageResource(R.drawable.ic_notification_pause)
        }

        binding.songMatch.setOnClickListener {

            URLs.matchupSongId = song.songCode
            URLs.matchupCheck = 1
            this.dismiss()
            replaceFragment(MatchSongs())

        }


        return binding.root

    }
    private fun replaceFragment(fragment: Fragment) {

        (activity as MainScreen).supportFragmentManager.beginTransaction().remove(RankSongs()).addToBackStack(null)
            .commit()
        (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer, fragment).addToBackStack(null)
            .commit()

    }

    fun adapterSet(){

        //adpter for comments
        recyclerViewComment = binding.recycleListComment

        recyclerViewComment.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        rvAdapterComment = RVAdapterComment(requireContext(),commentList,object : RVAdapterComment.OnItemClick {
            override fun onLiked(comment: GetCommentDataList, position: Int) {

                CoroutineScope(Dispatchers.IO).launch {
                    commentLikeUnlikeAction(comment.commentId,1)
                }

            }

            override fun onShare(comment: GetCommentDataList, position: Int) {
            }

            override fun onReply(comment: GetCommentDataList, position: Int) {

                commentParentId = comment.commentId

                val inputMethodManager: InputMethodManager? = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager?.showSoftInput(binding.comment,
                    InputMethodManager.SHOW_FORCED
                )
                binding.comment.requestFocus()



            }

            override fun onUnLiked(comment: GetCommentDataList, position: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    commentLikeUnlikeAction(comment.commentId,0)

                }
            }
        })

        recyclerViewComment.adapter = rvAdapterComment


//song win lose count adapter
        recyclerView = binding.recycleList
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


        rvAdapter = RVAdapterSongDetail(requireContext(),songsList,object : RVAdapterSongDetail.OnItemClick {
            override fun onClick(song: SongDetail, position: Int) {
            }
        })
        recyclerView.adapter = rvAdapter




    }

    private fun initLineChart() {

//        hide grid lines
        binding.graph.axisLeft.setDrawGridLines(false)
        binding.bigGraph.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = binding.graph.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)


        val x2Axis: XAxis = binding.bigGraph.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //left line of graph disable
        val leftAxis: YAxis = binding.bigGraph.axisLeft
        leftAxis.isEnabled = false


        val leftaxis: YAxis = binding.graph.axisLeft
        leftaxis.isEnabled = false

        //remove right y-axis
        binding.graph.axisRight.isEnabled = false
        binding.bigGraph.axisRight.isEnabled = false

        //remove legend
        binding.graph.legend.isEnabled = false
        binding.bigGraph.legend.isEnabled = false


        //remove description label
        binding.graph.description.isEnabled = false
        binding.bigGraph.description.isEnabled = false

        binding.graph.setTouchEnabled(false)
        binding.graph.isDoubleTapToZoomEnabled = false
        binding.graph.xAxis.isEnabled = false
        binding.graph.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.graph.xAxis.setDrawGridLines(false)
        binding.graph.invalidate()

        binding.bigGraph.isPinchZoomEnabled
        binding.bigGraph.setTouchEnabled(true)
        binding.bigGraph.isDoubleTapToZoomEnabled = false
        binding.bigGraph.xAxis.isEnabled = true
        binding.bigGraph.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.bigGraph.xAxis.setDrawGridLines(false)
        binding.bigGraph.invalidate()

    }

    //data set in graph and total win lose count
    fun setLineChartData() {

        val lineEntry = ArrayList<Entry>()

//        val set: Set<SongDetail> = HashSet(songsList)
//        songsList.clear()
//        songsList.addAll(set)
//        songsList.sortBy {
//            it.points
//        }


        songsList.sortBy {
            SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(it.createdDate).time
            //(it.createdDate)
        }

        songsList.reverse()
        rvAdapter.addData(songsList)

        for (item in songsList.indices) {
            if(songsList[item].status) {
                winCounter += 1
            } else {
                loseCounter += 1
            }
        }
        binding.winpt.text  = winCounter.toString()
        binding.losept.text = loseCounter.toString()


        val set2: ArrayList<SongDetail> = ArrayList()
        for ((index, item) in songsList.withIndex()) {
            if (index >= 1) {
                if (item.points.toInt() != songsList[index - 1].points.toInt()) {
                    set2.add(item)
                }
            } else {
                set2.add(item)
            }
        }

        for (item in set2.indices) {
            lineEntry.add(Entry(item.toFloat(), set2[item].points.toFloat()))
        }


        val lineDataSet = LineDataSet(lineEntry, "")
        lineDataSet.color = resources.getColor(R.color.orange)
        lineDataSet.valueTextColor = Color.WHITE
        val data = LineData(lineDataSet)


        Handler(Looper.getMainLooper()).postDelayed({

            binding.graph.data = data
            binding.graph.visibility = View.VISIBLE

        }, 500)



        binding.bigGraph.data = data

    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//
//    }


    private fun commentLikeUnlikeAction(commentId:Long,action:Long) {

        val checked = PostCommentAction(
            u_id!!,
            commentId,
            action,
            song.songCode!!,
            song.songTitle!!,
            song.image!!,
            song.artistTitle!!,
        )


        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(
            object : ApiHandler {
                override fun onSuccess(response: Any) {
                    loading.isDismiss()
                    response as CommentActionModel


                }

                override fun onFailure(t: Throwable) {
                    Log.e("test", "onFailure: ${t.message}")
                    Toast.makeText(
                        requireContext(),
                        "Api Syncing Failed comment like/unlike ..${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            },
            RetrofitClientUser(requireContext()).getRetrofitClientUser(false)
                .getCommentAction(gson)
        )
    }


    //post or reply comment of specific song
    private fun postApiCallingForComments(post:String,check:Long) {

        val checked = PostCommentModel(
            song.songCode!!,
            post,
            u_id!!,
            song.image!!,
            song.songTitle!!,
            song.artistTitle!!,
            check
        )


        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(
            object : ApiHandler {
                override fun onSuccess(response: Any) {
                    loading.isDismiss()
                    response as GetCommentModel
                    commentList.add(response.data.commentResponse)
                    makingChildList(commentList)

                }

                override fun onFailure(t: Throwable) {
                    Log.e("test", "onFailure: ${t.message}")
                    Toast.makeText(
                        requireContext(),
                        "Api Syncing Failed post comments ..${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            },
            RetrofitClientUser(requireContext()).getRetrofitClientUser(false)
                .postCommentResponse(gson)
        )
    }

//api for getting songs comment
    private fun apiCallingForSongComments() {

        val checked = GetCommentPost(
            song.songCode!!,
            500,
            u_id!!,
            1
        )


        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(
            object : ApiHandler {
                override fun onSuccess(response: Any) {
                    loading.isDismiss()
                     response as GetCommentModel
                    commentList.clear()
                    commentList.addAll(response.data.comments)

                    makingChildList(commentList)


                }

                override fun onFailure(t: Throwable) {
                    Log.e("test", "onFailure: ${t.message}")
                    Toast.makeText(
                        requireContext(),
                        "Api Syncing Failed comments ..${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            },
            RetrofitClientUser(requireContext()).getRetrofitClientUser(false)
                .getCommentResponse(gson)
        )
    }



//total win and lose count of songs details
    private fun apiCallingForSongDetails() {

        val checked = SongCheckpost(
            song!!.songCode!!,
            u_id!!
        )


        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(
            object : ApiHandler {
                override fun onSuccess(response: Any) {
                    loading.isDismiss()
                    status = response as SongDetailModel
                    songsList.clear()
                    songsList.addAll(response.data as List<SongDetail>)
                    setLineChartData()


                }

                override fun onFailure(t: Throwable) {
                    Log.e("test", "onFailure: ${t.message}")
                    Toast.makeText(
                        requireContext(),
                        "Api Syncing Failed songDetails ..${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            },
            RetrofitClientUser(requireContext()).getRetrofitClientUser(false)
                .getSongDetailResponse(gson)
        )
    }

//filtering parent and child comment
    private fun makingChildList(commentList: ArrayList<GetCommentDataList>) {
        parentList.clear()
        childList.clear()
        for (item in commentList) {
            if (item.parentId != 0L) {
                childList.add(item)
            } else {
                parentList.add(item)
            }
        }
        parentList.sortBy {
            it.createdDate
        }
        rvAdapterComment.addData(parentList, childList)
        Log.e("", "makingChildList: ")
    }

    override fun callFunction(p_id: String, off: Int, isLoadMore: Boolean) {

    }

    override fun callFunctionForAdap(commentId: Long, action: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            commentLikeUnlikeAction(commentId,action)
        }

    }

}