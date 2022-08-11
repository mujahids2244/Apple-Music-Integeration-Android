package com.arhamsoft.matchranker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.databinding.CardViewBinding
import com.arhamsoft.matchranker.databinding.CardViewFollowBinding
import com.arhamsoft.matchranker.databinding.CardViewWatchuserRecentactivityBinding
import com.arhamsoft.matchranker.models.FollowModelData
import com.arhamsoft.matchranker.models.FollowModelDataList
import com.arhamsoft.matchranker.models.SongCheckData
import com.arhamsoft.matchranker.models.WatchUserRecentactivity


class RVAdapterWatchUserRecentActivity(
    var context: Context,
    private var sList: ArrayList<WatchUserRecentactivity>,
    private var listenerClick: OnItemClick,

) : RecyclerView.Adapter<RVAdapterWatchUserRecentActivity.ViewHolder>() {


    private lateinit var binding: CardViewWatchuserRecentactivityBinding
    private var mContext: Context? = null
    var pic: String? = null
    var song_pic: String? = null


    fun addData(slist: ArrayList<WatchUserRecentactivity>) {
        this.sList = slist
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = CardViewWatchuserRecentactivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        mContext = parent.context

        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.itemView.setBackgroundColor(Color.parseColor("#FD6E3F"));

            val listPos = sList[position]
        if(listPos.playerImage != null) {
            if (listPos.playerImage.contains("data:image/jpeg;base64,") == true) {

                pic = listPos.playerImage.replace("data:image/jpeg;base64,", "")
            }

            if (pic != null) {
                val imageBytes: ByteArray = Base64.decode(pic!!, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.bind.userpic.setImageBitmap(decodedImage)
            }
        }
        if(listPos.songImage != null){

            if (listPos.songImage.contains("{w}x{h}bb")){

                song_pic = listPos.songImage.replace("{w}x{h}bb","100x100bb")
            }
            binding.songPic.load(song_pic) {
                placeholder(R.drawable.ic_baseline_library_music_24)
            }
        }

        when(listPos.action){

            0L -> { binding.description.text =  " ${listPos.playerName} has commented on ${listPos.songName}"}
            1L -> { binding.description.text =  " ${listPos.playerName} has liked a comment on ${listPos.songName}"}
            2L -> { binding.description.text =  " ${listPos.playerName} has commented on ${listPos.songName}"}
            3L -> { binding.description.text =  " ${listPos.playerName} has played ${listPos.songName}" }
            4L -> { binding.description.text =  " ${listPos.playerName} has liked a comment on ${listPos.songName}"}
            5L -> { binding.songDetail.visibility = View.GONE
                binding.description.text =  " ${listPos.playerName} started following you ${listPos.songName}"
                       }
        }

        holder.onBind(listPos,listenerClick, position)

        }


    override fun getItemCount(): Int = sList.size

    class ViewHolder(val bind: CardViewWatchuserRecentactivityBinding) : RecyclerView.ViewHolder(bind.root) {
        fun onBind(model: WatchUserRecentactivity, listener: OnItemClick, position: Int) {

            bind.songDetail.setOnClickListener {
                listener.onClick(model, position)
            }
        }
    }

    interface OnItemClick {

        fun onClick(userData: WatchUserRecentactivity,position: Int)


    }
}