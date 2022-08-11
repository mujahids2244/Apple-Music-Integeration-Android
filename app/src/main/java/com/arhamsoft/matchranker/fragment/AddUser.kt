package com.arhamsoft.matchranker.fragment

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapterAddUser
import com.arhamsoft.matchranker.adapter.RVAdapterFollow
import com.arhamsoft.matchranker.databinding.FragmentAddUserBinding
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
import com.tablitsolutions.crm.activities.RecyclerViewLoadMoreScroll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddUser : Fragment(), CallMethodOfFragFollow {

    lateinit var binding:FragmentAddUserBinding
    var u_id:String? = null
    private lateinit var rvAdapter: RVAdapterAddUser
    var userList: ArrayList<AddUserModelData> = ArrayList()
    lateinit var loading:LoadingDialog
    var watchUserActivtyList: ArrayList<WatchUserRecentactivity> = ArrayList()
    lateinit var rvLoadMore: RecyclerViewLoadMoreScroll




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddUserBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)
        CallMethodFragObject.setListener(this as CallMethodOfFragFollow )

        binding.recycleList.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL,false)

        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
        }

        binding.backtoaccount.setOnClickListener {
            replaceFragment(UserManagement())
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
                    getusers(newText!!.toString())
                    withContext(Dispatchers.Main){
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {
                Log.e("text", "afterTextChanged: ")
            }
        })




        rvAdapter = RVAdapterAddUser(requireContext(),userList,0,object : RVAdapterAddUser.OnItemClick {
            override fun onFollowClick(follow: AddUserModelData, position: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    getuserStatus(follow.playerId, 0)
                    withContext(Dispatchers.Main){
                        loading.startLoading("please Wait")
                    }


                }
                URLs.noOfFollowing +=1
                URLs.follStatusCheck = 1
            }

            override fun onUnFollowClick(follow: AddUserModelData, position: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    getuserStatus(follow.playerId, 1)
                    withContext(Dispatchers.Main){
                        loading.startLoading("please Wait")
                    }

                    URLs.noOfFollowing -=1
                    URLs.follStatusCheck = 1

                }

            }

            override fun onGotoProfile(follow: AddUserModelData, position: Int) {
                URLs.playerId = follow.playerId
                CoroutineScope(Dispatchers.IO).launch {
                    getWatchUserData(follow.playerId ,0 ,false)
                    withContext(Dispatchers.Main){
                        loading.startLoading("Please Wait")
                    }
                }
                URLs.callmethodofanotherfrag = 0
            }

            override fun unblockUser(follow: AddUserModelData, position: Int) {
            }
        })
        binding.recycleList.adapter = rvAdapter


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
                URLs.fragCheck = 3

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


    fun getusers(searchText:String) {

        val checked = AddUserPost(
            searchText,
            u_id!!
        )
        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                binding.progressBar.visibility = View.VISIBLE
                response as AddUserModel
                userList.clear()
                userList.addAll(response.data)
                rvAdapter.addData(userList)
                rvAdapter.notifyDataSetChanged()
//                if (isLoadMore) {
//                    rvLoadMore.setLoaded()
//                }

                binding.progressBar.visibility = View.GONE

            }

            override fun onFailure(t: Throwable) {
                binding.progressBar.visibility = View.VISIBLE

                Toast.makeText(
                    context,
                    "Api Syncing Failed follow..${t.message}",
                    Toast.LENGTH_LONG
                ).show()

            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).searchUsers(gson))
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