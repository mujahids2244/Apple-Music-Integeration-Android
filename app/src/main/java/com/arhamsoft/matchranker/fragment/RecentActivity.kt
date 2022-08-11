package com.arhamsoft.matchranker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapterWatchUserRecentActivity
import com.arhamsoft.matchranker.databinding.FragmentRecentActivityBinding
import com.arhamsoft.matchranker.interfaces.followFollowing.CallMethodFragObject
import com.arhamsoft.matchranker.interfaces.followFollowing.NotifyRecentActivity
import com.arhamsoft.matchranker.models.SongCheckData
import com.arhamsoft.matchranker.models.WatchUserRecentactivity
import com.arhamsoft.matchranker.network.URLs
import com.tablitsolutions.crm.activities.OnLoadMoreListener
import com.tablitsolutions.crm.activities.RecyclerViewLoadMoreScroll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.Url


class RecentActivity : Fragment(),NotifyRecentActivity {

    lateinit var binding:FragmentRecentActivityBinding
    var recentActivityList: ArrayList<WatchUserRecentactivity> = ArrayList()
    var p_id:String?= null
    private lateinit var rvAdapter: RVAdapterWatchUserRecentActivity
    lateinit var rvLoadMore: RecyclerViewLoadMoreScroll
    private var bottomFragment: SongDetailsBottomDialog? = null
    lateinit var song:SongCheckData


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecentActivityBinding.inflate(LayoutInflater.from(context))
        CallMethodFragObject.setListner(this)
        recentActivityList = URLs.watchUserActivityList

        p_id = URLs.playerId

        binding.recycleList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        initScrollListener()
        rvAdapter = RVAdapterWatchUserRecentActivity(requireContext(),recentActivityList,object : RVAdapterWatchUserRecentActivity.OnItemClick {
            override fun onClick(userData: WatchUserRecentactivity, position: Int) {
                URLs.currentSong = SongCheckData()
                URLs.currentSong!!.songCode = userData.songCode
                URLs.currentSong!!.image = userData.songImage
                URLs.currentSong!!.artistTitle = userData.artistTitle
                URLs.currentSong!!.songTitle = userData.songName
                bottomFragment = SongDetailsBottomDialog()
                (activity as MainScreen).showFragment(SongDetailsBottomDialog())
                URLs.fromRank = 0

            }
        })
        binding.recycleList.adapter = rvAdapter

        return binding.root
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

        binding.progressBar.visibility = View.VISIBLE
        URLs.callmethodofanotherfrag = 1
        CallMethodFragObject.passingData(p_id!!,recentActivityList.size,true)
    }

    override fun notifyFrag() {
        recentActivityList = URLs.watchUserActivityList
        binding.progressBar.visibility=View.GONE
        rvAdapter.addData(recentActivityList)
//        recentActivityList.clear()
    }

}