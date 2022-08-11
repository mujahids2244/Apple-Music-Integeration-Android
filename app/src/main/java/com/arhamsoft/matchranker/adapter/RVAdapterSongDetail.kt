package com.arhamsoft.matchranker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.databinding.CardViewBinding
import com.arhamsoft.matchranker.databinding.CardViewSongDetailBinding
import com.arhamsoft.matchranker.models.PlayedDataModel
import com.arhamsoft.matchranker.models.SongCheckData
import com.arhamsoft.matchranker.models.SongDetail


class RVAdapterSongDetail(
    var context: Context,
    private var sList: ArrayList<SongDetail>,
    private var listenerClick: OnItemClick,

) : RecyclerView.Adapter<RVAdapterSongDetail.ViewHolder>() {


    private lateinit var binding: CardViewSongDetailBinding
    private var mContext: Context? = null
    var pic: String? = null
    var selectedPosition = -1

    //searchbar
    fun addData(slist: ArrayList<SongDetail>) {
        this.sList = slist
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = CardViewSongDetailBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        mContext = parent.context
//        for (item in sList.indices) {
//            pic = sList[item].image
//            if (pic!!.contains("{w}x{h}bb")) {
//                pic = pic!!.replace("{w}x{h}bb", "200x200bb")
//            }
//        }
        return ViewHolder(binding)
    }
//
//    override fun getItemViewType(position: Int): Int {
//        return position
//    }

//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val listPos = sList[position]
            if (listPos.image.contains("{w}x{h}bb")) {
                pic = listPos.image.replace("{w}x{h}bb", "100x100bb")
            }
//        pic?.let {
//            Picasso.get().load(it).placeholder(R.drawable.ic_baseline_library_music_24)
//                .error(R.drawable.ic_baseline_library_music_24).into(binding.songPic)
//        }
            binding.songPic.load(pic){
                placeholder(R.drawable.ic_baseline_library_music_24)
            }
            holder.bind.tvSongName.text = listPos.songTitle
            holder.bind.tvArtisName.text = listPos.artistTitle

        if (listPos.status) {
            holder.bind.wlFlag.text = "W"
            holder.bind.wlFlag.setTextColor(Color.GREEN)
        }
        else
        {
            holder.bind.wlFlag.text = "L"
            holder.bind.wlFlag.setTextColor(Color.RED)

        }
            holder.onBind(listPos,listenerClick, position)
//        if (position == sList.size-1) {
//            listener?.onReachedBottom(position + 1)
//        }
        }


    override fun getItemCount(): Int = sList.size

    class ViewHolder(val bind: CardViewSongDetailBinding) : RecyclerView.ViewHolder(bind.root) {
        fun onBind(model: SongDetail, listener: OnItemClick, position: Int) {

            itemView.setOnClickListener {
                listener.onClick(model, position)
            }
        }
    }

    interface OnItemClick {
        fun onClick(song:SongDetail, position: Int)
    }
}