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
import com.arhamsoft.matchranker.models.AddUserModelData
import com.arhamsoft.matchranker.models.FollowModelData
import com.arhamsoft.matchranker.models.FollowModelDataList
import com.arhamsoft.matchranker.models.SongCheckData


class RVAdapterAddUser(
    var context: Context,
    private var sList: ArrayList<AddUserModelData>,
    var check:Int,
    private var listenerClick: OnItemClick,

) : RecyclerView.Adapter<RVAdapterAddUser.ViewHolder>() {


    private lateinit var binding: CardViewFollowBinding
    private var mContext: Context? = null
    var pic: String? = null


    fun addData(slist: ArrayList<AddUserModelData>) {
        this.sList = slist
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = CardViewFollowBinding.inflate(
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

        if (listPos.profileImage?.contains("data:image/jpeg;base64,") == true) {

            pic = listPos.profileImage.replace("data:image/jpeg;base64,", "")
        }
        if(check==1){
            holder.bind.unblock.visibility = View.VISIBLE
            holder.bind.follow.visibility = View.GONE
            holder.bind.following.visibility = View.GONE
        }
        else{
            if (listPos.isFollowing){
                holder.bind.follow.visibility = View.GONE
                holder.bind.following.visibility = View.VISIBLE
                holder.bind.unblock.visibility = View.GONE
            }
            else
            {
                holder.bind.follow.visibility = View.VISIBLE
                holder.bind.following.visibility = View.GONE
                holder.bind.unblock.visibility = View.GONE
            }
        }


        if (pic!=null) {
            val imageBytes: ByteArray = Base64.decode(pic!!, Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.bind.userpic.setImageBitmap(decodedImage)
        }
            holder.bind.tvusername.text = listPos.name

            holder.onBind(listPos,listenerClick, position)
//        if (position == sList.size-1) {
//            listener?.onReachedBottom(position + 1)
//        }
        }


    override fun getItemCount(): Int = sList.size

    class ViewHolder(val bind: CardViewFollowBinding) : RecyclerView.ViewHolder(bind.root) {
        fun onBind(model: AddUserModelData, listener: OnItemClick, position: Int) {

            bind.follow.setOnClickListener {
                listener.onFollowClick(model, position)
                bind.follow.visibility = View.GONE
                bind.following.visibility = View.VISIBLE
            }

            bind.following.setOnClickListener {
                listener.onUnFollowClick(model,position)
                bind.follow.visibility = View.VISIBLE
                bind.following.visibility = View.GONE
            }

            bind.gotoProfile.setOnClickListener {
                listener.onGotoProfile(model,position)
            }

            bind.unblock.setOnClickListener {
                listener.unblockUser(model,position)
            }


        }
    }



    interface OnItemClick {

        fun onFollowClick(follow: AddUserModelData,position: Int)
        fun onUnFollowClick(follow: AddUserModelData,position: Int)
        fun onGotoProfile(follow: AddUserModelData,position: Int)
        fun unblockUser(follow: AddUserModelData,position: Int)

    }
}