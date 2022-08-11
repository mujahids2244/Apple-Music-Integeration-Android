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
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapter
import com.arhamsoft.matchranker.adapter.RVAdapterFollow
import com.arhamsoft.matchranker.databinding.FragmentFollowersBinding
import com.arhamsoft.matchranker.databinding.FragmentFollowingBinding
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


class Following : Fragment(),CallMethodOfFragFollow {
    lateinit var binding: FragmentFollowingBinding
    var u_id: String? = null
    lateinit var loading: LoadingDialog
    var followingList: ArrayList<FollowModelDataList> = ArrayList()
    var watchUserActivtyList: ArrayList<WatchUserRecentactivity> = ArrayList()

    private lateinit var rvAdapter: RVAdapterFollow
    lateinit var rvLoadMore: RecyclerViewLoadMoreScroll


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = FragmentFollowingBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)

        CallMethodFragObject.setListener(this as CallMethodOfFragFollow )


        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
            getuserFollowing(0,false)
            withContext(Dispatchers.Main) {
                loading.startLoading("Please Wait")
            }
        }

        binding.backtoaccount.setOnClickListener {
            (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).commit()
            replaceFragment(Account())
        }

        binding.recycleList.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)

        initScrollListener()
        rvAdapter = RVAdapterFollow(requireContext(),followingList,0,object : RVAdapterFollow.OnItemClick {
            override fun onFollowClick(follow: FollowModelDataList, position: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    getuserStatus(follow.playerId, 0)
                    withContext(Dispatchers.Main){
                        loading.startLoading("please Wait")
                    }

                    URLs.noOfFollowing +=1
                    URLs.follStatusCheck = 1

                }
            }

            override fun onUnFollowClick(follow: FollowModelDataList, position: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    getuserStatus(follow.playerId, 1)
                    withContext(Dispatchers.Main){
                        loading.startLoading("please Wait")
                    }

                    URLs.noOfFollowing -=1
                    URLs.follStatusCheck = 1
                }
            }

            override fun onGotoProfile(follow: FollowModelDataList, position: Int) {
                URLs.playerId = follow.playerId
                CoroutineScope(Dispatchers.IO).launch {
                    getWatchUserData(follow.playerId ,0 ,false)
                    withContext(Dispatchers.Main){
                        loading.startLoading("Please Wait")
                    }
                }
                URLs.callmethodofanotherfrag = 0

            }

            override fun unblockUser(follow: FollowModelDataList, position: Int) {
            }
        })

        binding.recycleList.adapter = rvAdapter
        return binding.root
    }


    private fun getWatchUserData(p_id:String, off: Int, isLoadMore: Boolean) {

        val checked = WatchUserModelPost(
            p_id!!,
            50,
            off.toLong()+1
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                response as WatchUsersModel
                URLs.watchUserResponse = response
                watchUserActivtyList.addAll(response.data.recentactivity)
                URLs.watchUserActivityList = watchUserActivtyList

                if(URLs.callmethodofanotherfrag ==0) {
                    replaceFragment(UserProfile())
                }
                else{
                    CallMethodFragObject.passNotify()
                }
                URLs.fragCheck = 2


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

    private fun getuserStatus(playerId:String, connectivityType:Long) {

        val checked = UserStatusModelPost(
            u_id!!,
            connectivityType,
            playerId
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                response as FollowModel


            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()

                Log.e("testgetsongsfromserver", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed user status..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).userStatus(gson))
    }


    private fun getuserFollowing(off:Int,isLodeMore: Boolean) {

        val checked = FollowModelPost(
            u_id!!,
            1,
            30,
            off.toLong() + 1
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                response as FollowModel
                followingList.addAll(response.data.players)
                rvAdapter.addData(followingList)
                binding.recycleList.adapter?.notifyDataSetChanged()
                if (isLodeMore){
                    rvLoadMore.setLoaded()
                }
                binding.progressBar.visibility = View.GONE

            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("testgetsongsfromserver", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed following..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                CoroutineScope(Dispatchers.IO).launch {
                    getuserFollowing(0,false)

                }
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).usersFollow(gson))
    }

    private fun replaceFragment(fragment: Fragment) {

        (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer,fragment).addToBackStack(null).commit();

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
            getuserFollowing(followingList.size,true)
        }
    }

    override fun callFunction(p_id: String, off: Int, isLoadMore: Boolean) {
        getWatchUserData(p_id, off, isLoadMore)

    }

    override fun callFunctionForAdap(commentId: Long, action: Long) {

    }

}