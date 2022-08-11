package com.arhamsoft.matchranker.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapter
import com.arhamsoft.matchranker.databinding.FragmentRankSongsInUPBinding
import com.arhamsoft.matchranker.interfaces.Communicator
import com.arhamsoft.matchranker.models.RankSongCheck
import com.arhamsoft.matchranker.models.SongCheckData
import com.arhamsoft.matchranker.models.WatchUserRankSongsPost
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.URLs
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tablitsolutions.crm.activities.OnLoadMoreListener
import com.tablitsolutions.crm.activities.RecyclerViewLoadMoreScroll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RankSongsInUP : Fragment() {

    lateinit var binding: FragmentRankSongsInUPBinding
    private lateinit var rvAdapter: RVAdapter
    private lateinit var communicator: Communicator

    var u_id :String? = null
    var p_id :String? = null
     var songsList:ArrayList<SongCheckData> = ArrayList()
    lateinit var loading:LoadingDialog
    lateinit var rvLoadMore: RecyclerViewLoadMoreScroll
    private var bottomFragment: SongDetailsBottomDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRankSongsInUPBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)
        communicator = requireActivity() as Communicator

        p_id = URLs.playerId
        binding.recycleList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
            getWatchUserRankSongs(0,false)
            withContext(Dispatchers.Main){
                loading.startLoading("Please wait")
            }
        }

        initScrollListener()
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

//                bottomFragment!!.show( MainScreen.activity.supportFragmentManager.beginTransaction(),"frag2")

//            val intent = Intent(this, BottomSheetPlayer::class.java)
//                val intent = Intent(requireContext(), SongDetails::class.java)
//                intent.putExtra("songObj", song)
//                startActivity(intent)
            }

        })
        binding.recycleList.adapter = rvAdapter


        return binding.root
    }

        private fun getWatchUserRankSongs(off:Int,isLodeMore: Boolean) {

        val checked = WatchUserRankSongsPost(
            u_id!!,
            p_id!!,
            30,
            off.toLong()+1
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                response as RankSongCheck
                songsList.addAll(response.data as List<SongCheckData>)
                rvAdapter.addData(songsList)
                (activity as MainScreen).setSongsData(songsList)
                if (isLodeMore){
                    rvLoadMore.setLoaded()
                }

                binding.progressBar.visibility = View.GONE

            }

            override fun onFailure(t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Api Syncing Failed WatchUser RAnk Songs..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).getWatchUserRankSongs(gson))
    }


    private fun initScrollListener(){
        rvLoadMore = RecyclerViewLoadMoreScroll(binding.recycleList.layoutManager as LinearLayoutManager)
        rvLoadMore.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                loadMore()
            }
        })
        binding.recycleList.addOnScrollListener(rvLoadMore)

    }


    private fun loadMore(){
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                binding.progressBar.visibility = View.VISIBLE
            }
            getWatchUserRankSongs(songsList.size,true)

        }
    }

}