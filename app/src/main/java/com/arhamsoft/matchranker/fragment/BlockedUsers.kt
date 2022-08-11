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
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapterFollow
import com.arhamsoft.matchranker.databinding.FragmentBlockedUsersBinding
import com.arhamsoft.matchranker.models.FollowModel
import com.arhamsoft.matchranker.models.FollowModelDataList
import com.arhamsoft.matchranker.models.FollowModelPost
import com.arhamsoft.matchranker.models.UserStatusModelPost
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.room.UserDatabase
import com.arhamsoft.matchranker.usermodels.RetrofitClientUser
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BlockedUsers : Fragment() {

    lateinit var binding:FragmentBlockedUsersBinding
    lateinit var loading:LoadingDialog
    var u_id:String? = null
    var blockedList: ArrayList<FollowModelDataList> = ArrayList()
    lateinit var rvAdapter: RVAdapterFollow

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlockedUsersBinding.inflate(LayoutInflater.from(context))
        loading = LoadingDialog(requireContext() as Activity)


        binding.backtoaccount.setOnClickListener {
            replaceFragment(UserManagement())
        }
        binding.recycleList.layoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL,false)


        CoroutineScope(Dispatchers.IO).launch {
            val user = UserDatabase.getDatabase(requireContext()).userDao().getUser()
            u_id = user.userId
            getBlockedUsers(0,false)
              withContext(Dispatchers.Main){
                loading.startLoading("Please Wait")
            }
        }

        rvAdapter = RVAdapterFollow(requireContext(),blockedList,1,object : RVAdapterFollow.OnItemClick {
            override fun onFollowClick(follow: FollowModelDataList, position: Int) {
            }

            override fun onUnFollowClick(follow: FollowModelDataList, position: Int) {
            }

            override fun onGotoProfile(follow: FollowModelDataList, position: Int) {
            }

            override fun unblockUser(follow: FollowModelDataList, position: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    getuserStatus(follow.playerId,3,position)
                    withContext(Dispatchers.Main){
                        loading.startLoading("Please Wait")
                    }
                }
            }
        })
        binding.recycleList.adapter = rvAdapter

        return binding.root


    }

    private fun getuserStatus(playerId:String, connectivityType:Long,pos:Int) {

        val checked = UserStatusModelPost(
            u_id!!,
            connectivityType,
            playerId
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()

                blockedList.removeAt(pos)
                binding.recycleList.adapter?.notifyItemRemoved(pos)
                binding.recycleList.adapter?.notifyDataSetChanged()
                response as FollowModel

            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Log.e("testgetsongsfromserver", "onFailure: ${t.message}")
                Toast.makeText(
                    context,
                    "Api Syncing Failed user status unblock user..${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).userStatus(gson))
    }


    fun getBlockedUsers(off: Int, isLoadMore: Boolean) {

        val checked = FollowModelPost(
            u_id!!,
            2,
            30,
            off.toLong()+1
        )

        val gson: JsonObject = JsonParser.parseString(Gson().toJson(checked)).asJsonObject

        APIResult(object : ApiHandler {
            override fun onSuccess(response: Any) {
                loading.isDismiss()
                response as FollowModel
                blockedList.addAll(response.data.players)
                rvAdapter.addData(blockedList)
                binding.recycleList.adapter?.notifyDataSetChanged()
//                if (isLoadMore) {
//                    rvLoadMore.setLoaded()
//                }

                binding.progressBar.visibility = View.GONE

            }

            override fun onFailure(t: Throwable) {
                loading.isDismiss()
                Toast.makeText(
                    context,
                    "Api Syncing Failed blocked users..${t.message}",
                    Toast.LENGTH_LONG
                ).show()

                CoroutineScope(Dispatchers.IO).launch {
                    getBlockedUsers(0,false)

                }
            }
        }, RetrofitClientUser(requireContext()).getRetrofitClientUser(false).usersFollow(gson))
    }

    private fun replaceFragment(fragment: Fragment) {

        (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).addToBackStack(null)
            .commit()
        (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer, fragment).addToBackStack(null)
            .commit()

    }

}