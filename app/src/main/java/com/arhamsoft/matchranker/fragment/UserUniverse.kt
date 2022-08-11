package com.arhamsoft.matchranker.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapter
import com.arhamsoft.matchranker.databinding.FragmentAccountBinding
import com.arhamsoft.matchranker.databinding.FragmentEditProfileBinding
import com.arhamsoft.matchranker.databinding.FragmentRankSongsBinding
import com.arhamsoft.matchranker.databinding.FragmentUserUniverseBinding
import com.arhamsoft.matchranker.models.*
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
import org.json.JSONObject
import java.util.ArrayList


class UserUniverse : Fragment(R.layout.fragment_user_universe) ,SwipeRefreshLayout.OnRefreshListener {

    lateinit var binding: FragmentUserUniverseBinding
    var u_id: String? = null
    lateinit var loading: LoadingDialog
    lateinit var recyclerView: RecyclerView
    lateinit var rvLoadMore: RecyclerViewLoadMoreScroll
    private lateinit var rvAdapter: RVAdapter
    var songsList: ArrayList<SongCheckData> = ArrayList()
    var statusRejected: RejectedResponse? = null






    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = FragmentUserUniverseBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)
        recyclerView = binding.recycleList
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.refreshList.setOnRefreshListener(this)

        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
            getRejectedSongsFromServer(0, false)
            withContext(Dispatchers.Main) {
                loading.startLoading("Please Wait")

            }

        }


        initScrollListener()
        rvAdapter = RVAdapter(requireContext(), songsList,2, object : RVAdapter.OnItemClick {
            override fun onClick(song: SongCheckData, position: Int) {
//                loading.startLoading("Please Wait")
            Toast.makeText(requireContext(),"clicked", Toast.LENGTH_SHORT).show()
//                loading.isDismiss()

            }

            override fun onbtnClick(song: SongCheckData, position: Int) {
//                URLs.currentSong = song
                rejectSongApi(song,false,position)
            }

            override fun onForDetailClick(song: SongCheckData, position: Int) {

            }

        })
        recyclerView.adapter = rvAdapter

        binding.gotoGenere.setOnClickListener {
            (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).addToBackStack(null).commit();
            replaceFragment(Filters())

        }

        binding.backtoaccount.setOnClickListener {
            (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).addToBackStack(null).commit();
            replaceFragment(Account())
        }
        return binding.root
    }

    private fun rejectSongApi(songData: SongCheckData, check: Boolean, pos:Int) {

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




        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {

                songsList.removeAt(pos)
                binding.recycleList.adapter?.notifyItemRemoved(pos)
                binding.recycleList.adapter?.notifyDataSetChanged()

                statusRejected = response as RejectedResponse
                Toast.makeText(
                    context,
                    "revert",
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


    private fun getRejectedSongsFromServer(off: Int, isLoadMore: Boolean) {


        val checked = RankSongpost(
            10,
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
                val songModel = response as RankSongCheck
                songsList.addAll(songModel.data as List<SongCheckData>)
                binding.recycleList.adapter?.notifyDataSetChanged()
                if (isLoadMore) {
                    rvLoadMore.setLoaded()
                }
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    context,
                    "reject list success",
                    Toast.LENGTH_LONG
                ).show()
                binding.refreshList.isRefreshing = false

//
//                Log.e("server", "onSuccess: ${songModel}")

            }

            override fun onFailure(t: Throwable) {
                Log.e("testgetsongsfromserver", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed get rejecteds songs..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).getRejectedSongs(gson))
    }

    private fun initScrollListener() {
        rvLoadMore = RecyclerViewLoadMoreScroll(binding.recycleList.layoutManager as LinearLayoutManager)
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
            getRejectedSongsFromServer((songsList.size), true)

        }

    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentTransaction = (activity as MainScreen).supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragContainer, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }

    override fun onRefresh() {
        binding.refreshList.isRefreshing = true
//        songsList.remove(URLs.currentSong)
        CoroutineScope(Dispatchers.IO).launch {
            getRejectedSongsFromServer(songsList.size, false)
        }
    }


}