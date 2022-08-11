package com.arhamsoft.matchranker.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import android.util.Base64
import android.view.View
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.activity.LoadingDialog
import com.arhamsoft.matchranker.databinding.CardViewCommentsListBinding
import com.arhamsoft.matchranker.interfaces.followFollowing.CallMethodFragObject
import com.arhamsoft.matchranker.interfaces.followFollowing.CallMethodOfFragFollow
import com.arhamsoft.matchranker.models.GetCommentDataList
import java.nio.charset.StandardCharsets
import java.security.spec.PSSParameterSpec.DEFAULT
import java.util.*
import java.util.Base64.getDecoder
import kotlin.collections.ArrayList


class RVAdapterComment(
    var context: Context,
    private var sList: ArrayList<GetCommentDataList>,
    private var listenerClick: OnItemClick,
    ) : RecyclerView.Adapter<RVAdapterComment.ViewHolder>() {
    private lateinit var binding: CardViewCommentsListBinding
    private var mContext: Context? = null
    var pic: String? = null
    lateinit var recyclerViewChildComment: RecyclerView
    private lateinit var rvAdapterChildComment: RVAdapterCommentChild
    private var childList: ArrayList<GetCommentDataList> = ArrayList()

    var parentList: ArrayList<GetCommentDataList> = ArrayList()


    fun addData(slist: ArrayList<GetCommentDataList>, childList: ArrayList<GetCommentDataList>) {
        this.sList = slist
        this.childList = childList
        notifyDataSetChanged()
    }

    fun addDataPC(slist: ArrayList<GetCommentDataList>) {
        for (item in slist.indices){

            if(slist[item].parentId == 0L){
                this.sList = slist
            }
            else{
                this.childList = slist
            }

        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = CardViewCommentsListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        mContext = parent.context



//        this.mContext = context
        return ViewHolder(binding)
    }

//    override fun getItemViewType(position: Int): Int {
//        return position
//    }
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val updatedChildList: ArrayList<GetCommentDataList> = ArrayList()

//        updatedChildList.clear()

        //child comments acc to their parent
        if (childList != null && childList.isNotEmpty()) {
            for (item in childList) {
                if (item.parentId == sList[position].commentId) {
                    updatedChildList.add(item)

                }
            }
        }
//        rvAdapterChildComment2.addData(childList)

        val listPos = sList[position]


            holder.bind.childComment.layoutManager =
            LinearLayoutManager(holder.bind.childComment.context, LinearLayoutManager.VERTICAL, false)


        holder.bind.childComment.adapter  =
            RVAdapterCommentChild(holder.bind.childComment.context, updatedChildList, object : RVAdapterCommentChild.OnItemClick {
                override fun onLiked(comment: GetCommentDataList, position: Int) {
                    listenerClick.onLiked(comment, position)

                }

                override fun onUnLiked(comment: GetCommentDataList, position: Int) {
                    listenerClick.onUnLiked(comment, position)
                }

                override fun onShare(comment: GetCommentDataList, position: Int) {

                }
            })
//        (holder.bind.childComment.adapter as RVAdapterCommentChild).notifyItemChanged(position)
//        (holder.bind.childComment.adapter as RVAdapterCommentChild).notifyItemInserted(position)
        (holder.bind.childComment.adapter as RVAdapterCommentChild).notifyDataSetChanged()



        if (listPos.playerImage?.contains("data:image/jpeg;base64,") == true) {

            pic = listPos.playerImage.replace("data:image/jpeg;base64,", "")
        }

        if (listPos.isLiked){
            holder.bind.unlike.visibility = View.VISIBLE

        }
        val imageBytes: ByteArray = Base64.decode(pic, Base64.DEFAULT)
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        binding.userPic.setImageBitmap(decodedImage)

        holder.bind.tvComment.text = listPos.comment
        holder.bind.tvUserName.text = listPos.playerName

        holder.onBind(listPos, listenerClick, position)

    }


    override fun getItemCount(): Int = sList.size

    class ViewHolder(val bind: CardViewCommentsListBinding) : RecyclerView.ViewHolder(bind.root) {
        fun onBind(model: GetCommentDataList, listener: OnItemClick, position: Int) {

            bind.liked.setOnClickListener {
                listener.onLiked(model, position)

                    bind.unlike.visibility = View.VISIBLE
            }

            bind.unlike.setOnClickListener {
                listener.onUnLiked(model,position)
                bind.unlike.visibility = View.GONE

            }

            bind.share.setOnClickListener {

                listener.onShare(model, position)
            }

            bind.reply.setOnClickListener {
                listener.onReply(model, position)
            }
        }
    }


    interface OnItemClick {
        fun onLiked(comment: GetCommentDataList, position: Int)
        fun onShare(comment: GetCommentDataList, position: Int)
        fun onReply(comment: GetCommentDataList, position: Int)
        fun onUnLiked(comment: GetCommentDataList,position: Int)

    }
}