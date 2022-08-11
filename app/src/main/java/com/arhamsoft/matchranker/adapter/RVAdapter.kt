package com.arhamsoft.matchranker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.databinding.CardViewBinding
import com.arhamsoft.matchranker.models.SongCheckData


class RVAdapter(
    var context: Context,
    private var sList: ArrayList<SongCheckData>,
    private var check: Int,
//    private var songListener: SongListener,
    private var listenerClick: OnItemClick,

//                var mediaItem: MediaBrowserCompat.MediaItem? = null

) : RecyclerView.Adapter<RVAdapter.ViewHolder>() {


    private lateinit var binding: CardViewBinding
    private var mContext: Context? = null
    var pic: String? = null
    var selectedPosition = -1

    //searchbar
    fun addData(slist: ArrayList<SongCheckData>) {
        this.sList = slist
        notifyDataSetChanged()
//        Handler(Looper.getMainLooper()).post(Runnable {
//            notifyDataSetChanged()
//        })


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = CardViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
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
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.itemView.setBackgroundColor(Color.parseColor("#FD6E3F"));
            holder.setIsRecyclable(false)
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FD6E3F"))
        } else {
            holder.itemView.setBackgroundColor(
                Color.parseColor("#151723")
            )
        }
        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }


            val listPos = sList[position]


            if (listPos.image!!.contains("{w}x{h}bb")) {
                pic = listPos.image!!.replace("{w}x{h}bb", "100x100bb")
            }
//        pic?.let {
//            Picasso.get().load(it).placeholder(R.drawable.ic_baseline_library_music_24)
//                .error(R.drawable.ic_baseline_library_music_24).into(binding.songPic)
//        }
            binding.songPic.load(pic) {
                placeholder(R.drawable.ic_baseline_library_music_24)
            }

            holder.bind.tvSongName.text = listPos.songTitle
            holder.bind.tvArtisName.text = listPos.artistTitle
            if (check == 1) {
                holder.bind.rowno.text = (position + 1).toString()
                val number = listPos.points
                val rounded = String.format("%.2f", number)
                holder.bind.points.text = rounded
                holder.bind.revert.visibility = View.GONE
                holder.bind.songMatch.visibility = View.GONE

            } else if (check == 2) {
                holder.bind.rowno.text = (position + 1).toString()
                holder.bind.revert.visibility = View.VISIBLE
                holder.bind.points.visibility = View.GONE
                holder.bind.drag.visibility = View.GONE
            } else {
                holder.bind.revert.visibility = View.GONE
                holder.bind.points.visibility = View.GONE
                holder.bind.rowno.visibility = View.GONE
                holder.bind.drag.visibility = View.GONE
                holder.bind.songMatch.visibility = View.GONE

            }


            holder.onBind(listPos, listenerClick, position)
        }
//
        //        if (position == sList.size-1) {
//            listener?.onReachedBottom(position + 1)
//        }



    override fun getItemCount(): Int = sList.size

    class ViewHolder(val bind: CardViewBinding) : RecyclerView.ViewHolder(bind.root) {

        fun onBind(model: SongCheckData, listener: OnItemClick, position: Int) {

            bind.revert.setOnClickListener {
                listener.onbtnClick(model, position)

            }
            bind.songPic.setOnClickListener {

                listener.onClick(model, position)
//                songListener.onMediaItemClicked(mediaItem)
            }

            bind.gotoSongDetail.setOnClickListener {

                listener.onForDetailClick(model, position)
            }
        }
    }


    interface OnItemClick {
        fun onClick(song: SongCheckData, position: Int)
        fun onbtnClick(song: SongCheckData, position: Int)
        fun onForDetailClick(song: SongCheckData, position: Int)

    }
}