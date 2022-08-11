package com.arhamsoft.matchranker.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapterUserRecentActivity
import com.arhamsoft.matchranker.adapter.RVAdapterWatchUserRecentActivity
import com.arhamsoft.matchranker.databinding.FragmentActivityBinding
import com.arhamsoft.matchranker.interfaces.followFollowing.CallMethodFragObject
import com.arhamsoft.matchranker.interfaces.followFollowing.CallMethodOfFragFollow
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


class UserActivity : Fragment(), CallMethodOfFragFollow {

    lateinit var binding: FragmentActivityBinding
    var u_id: String? = null
    lateinit var loading: LoadingDialog
    lateinit var rvLoadMore: RecyclerViewLoadMoreScroll
    lateinit var rvAdapter:RVAdapterUserRecentActivity
    var activityList:ArrayList<UserActivityModelData> = ArrayList()
    private var bottomFragment: SongDetailsBottomDialog? = null
    var watchUserActivtyList: ArrayList<WatchUserRecentactivity> = ArrayList()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivityBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)

        CallMethodFragObject.setListener(this as CallMethodOfFragFollow )

        binding.backtoaccount.setOnClickListener {
            replaceFragment(Account())
        }

        binding.recycleList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        initScrollListener()
        rvAdapter = RVAdapterUserRecentActivity(requireContext(),activityList,object : RVAdapterUserRecentActivity.OnItemClick {
            override fun onClick(userData: UserActivityModelData, position: Int) {
                URLs.playerId = userData.playerId
                CoroutineScope(Dispatchers.IO).launch {
                    getWatchUserData(userData.playerId ,0 ,false)
                    withContext(Dispatchers.Main){
                        loading.startLoading("Please Wait")
                    }
                }
                URLs.callmethodofanotherfrag = 0

            }

            override fun onSongDetails(userData: UserActivityModelData, position: Int) {
                URLs.currentSong = SongCheckData()
                URLs.currentSong!!.songCode = userData.songCode
                URLs.currentSong!!.image = userData.songImage
                URLs.currentSong!!.artistTitle = userData.artistTitle
                URLs.currentSong!!.songTitle = userData.songName
                bottomFragment = SongDetailsBottomDialog()
                (activity as MainScreen).showFragment(SongDetailsBottomDialog())
                URLs.fromRank = 1
            }
        })
        binding.recycleList.adapter = rvAdapter



        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
            getUserActivity(0, false)
            withContext(Dispatchers.Main) {
                loading.startLoading("Please Wait")

            }

        }


        return binding.root
    }

    fun getWatchUserData(p_id:String, off: Int, isLoadMore: Boolean) {

        val checked = WatchUserModelPost(
            p_id!!,
            50,
            off.toLong() +1
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                response as WatchUsersModel
                URLs.watchUserResponse = response
                watchUserActivtyList.addAll(response.data.recentactivity)
                URLs.watchUserActivityList += watchUserActivtyList
                if (URLs.callmethodofanotherfrag == 0) {
                    replaceFragment(UserProfile())
                }
                else{
                    CallMethodFragObject.passNotify()
                }
                URLs.fragCheck = 4

                if(isLoadMore){
                    rvLoadMore.setLoaded()
                }

//                binding.progressBar.visibility = View.GONE



            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()

                Toast.makeText(
                    context,
                    "Api Syncing Failed WatchUser..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).watchUser(gson))
    }


    private fun getUserActivity(off: Int, isLoadMore: Boolean) {


        val checked = RankSongpost(
            20,
            off + 1,
            u_id!!
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                 response as UserActivityModel
                activityList.addAll(response.data as List<UserActivityModelData>)
                rvAdapter.addData(activityList)

                if(isLoadMore) {
                    rvLoadMore.setLoaded()
                }
                binding.progressBar.visibility = View.GONE


            }

            override fun onFailure(t: Throwable) {
                Log.e("testgetsongsfromserver", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed get rejecteds songs..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).getUserActivity(gson))
    }

    private fun initScrollListener() {
        rvLoadMore = RecyclerViewLoadMoreScroll(binding.recycleList.layoutManager as LinearLayoutManager)
        rvLoadMore.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                loadMore()
            }
        })
        binding.recycleList.addOnScrollListener(rvLoadMore)

    }

    private fun loadMore() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.VISIBLE
            }
            getUserActivity((activityList.size), true)

        }

    }


    private fun replaceFragment(fragment: Fragment) {

        (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).addToBackStack(null)
            .commit()
        (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer, fragment).addToBackStack(null)
            .commit()

    }

    override fun callFunction(p_id: String, off: Int, isLoadMore: Boolean) {
        getWatchUserData(p_id, off, isLoadMore)

    }

    override fun callFunctionForAdap(commentId: Long, action: Long) {
    }

}