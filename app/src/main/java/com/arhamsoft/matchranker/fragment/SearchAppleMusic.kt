package com.arhamsoft.matchranker.fragment

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
import androidx.recyclerview.widget.RecyclerView
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.MainScreen
import com.arhamsoft.matchranker.adapter.RVAdapter
import com.arhamsoft.matchranker.adapter.RVAdapterSearchAppleMusic
import com.arhamsoft.matchranker.databinding.*
import com.arhamsoft.matchranker.fragment.matchSong.MatchSongs
import com.arhamsoft.matchranker.interfaces.Communicator
import com.arhamsoft.matchranker.models.PlayedDataModel
import com.arhamsoft.matchranker.models.PlayedModel
import com.arhamsoft.matchranker.models.PlayedSearchModel
import com.arhamsoft.matchranker.models.SongCheckData
import com.arhamsoft.matchranker.network.APIResult
import com.arhamsoft.matchranker.network.ApiHandler
import com.arhamsoft.matchranker.network.RetrofitClient
import com.arhamsoft.matchranker.network.URLs
import com.tablitsolutions.crm.activities.RecyclerViewLoadMoreScroll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList


class SearchAppleMusic : Fragment() {

    lateinit var binding: FragmentSearchAppleMusicBinding
    var tempsongsList: ArrayList<PlayedDataModel> = ArrayList()
    lateinit var recyclerView: RecyclerView
    private lateinit var communicator: Communicator

    var songsList: ArrayList<PlayedDataModel> = ArrayList()

    private lateinit var rvAdapter: RVAdapterSearchAppleMusic

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        binding = FragmentSearchAppleMusicBinding.inflate(LayoutInflater.from(context))
        communicator = requireActivity() as Communicator
        recyclerView = binding.recycleList
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


        binding.backtoaccount.setOnClickListener {
            (activity as MainScreen).supportFragmentManager.beginTransaction().remove(this).commit()
            replaceFragment(Account())
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
                   callAppleMusicSearchSongs(newText!!.toString())
                    withContext(Dispatchers.Main){
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {
                Log.e("text", "afterTextChanged: ")
            }
        })

        rvAdapter = RVAdapterSearchAppleMusic(requireContext(),songsList,0, object : RVAdapterSearchAppleMusic.OnItemClick {
            override fun onClick(song: PlayedDataModel, position: Int) {

                communicator.passDataFromSearch(song, position,2)

            }

            override fun onbtnClick(song: PlayedDataModel, position: Int) {

            }

            override fun onMatchedSong(song: PlayedDataModel, position: Int) {
                URLs.matchupSongSearchApple = song
                URLs.matchupCheck = 2
                replaceFragment(MatchSongs())
            }

        })
        recyclerView.adapter = rvAdapter

        return binding.root
    }


    private suspend fun callAppleMusicSearchSongs(songTitle:String) {

            val url = URLs.searchFromAppleMusic + songTitle
            APIResult(object : ApiHandler {
                override fun onSuccess(response: Any) {
                    binding.progressBar.visibility = View.GONE
                    val playedModel = response as PlayedSearchModel
                    songsList.clear()
                    songsList.addAll( playedModel.results.songs.data as List<PlayedDataModel>)
                    (activity as MainScreen).setSongsDataForSearch(songsList)
                    tempsongsList = ArrayList()

                    for (i in 0 until playedModel.results.songs.data!!.size) {
//                    if (songModel.data[i].songTitle.contains(newText!!, true)) {
                        val songs = playedModel.results.songs.data[i]
                        tempsongsList.add(songs)
//                    }
                    }
                    rvAdapter.addData(tempsongsList)
                    rvAdapter.notifyDataSetChanged()

                    Log.e("userplay", "onSuccess:${URLs.songsArray} ")

                }

                override fun onFailure(t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Api Syncing Failed search apple music..${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("test", "onFailure: ${t.message}")
                }
            }, RetrofitClient(requireContext()).getRetrofitClient().getSongsFromAppleMusic(url))
        }

    private fun replaceFragment(fragment: Fragment) {
        (activity as MainScreen).supportFragmentManager.beginTransaction().remove(SearchAppleMusic()).addToBackStack(null).commit();

        (activity as MainScreen).supportFragmentManager.beginTransaction().replace(R.id.fragContainer,fragment).addToBackStack(null).commit();

    }

}