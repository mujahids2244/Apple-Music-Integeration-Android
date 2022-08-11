package com.arhamsoft.matchranker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.arhamsoft.matchranker.R
import com.arhamsoft.matchranker.databinding.CardViewCommentsChildListBinding
import com.arhamsoft.matchranker.databinding.CardViewCommentsListBinding
import com.arhamsoft.matchranker.models.GetCommentDataList
import com.arhamsoft.matchranker.models.SongDetail
import java.nio.charset.StandardCharsets
import java.security.spec.PSSParameterSpec.DEFAULT
import java.util.*


class RVAdapterCommentChild(
    var context: Context,
    private var sList: ArrayList<GetCommentDataList>,
    private var listenerClick: OnItemClick,


) : RecyclerView.Adapter<RVAdapterCommentChild.ViewHolder>() {


    private lateinit var binding: CardViewCommentsChildListBinding
    private var mContext: Context? = null
    var pic: String?= null



    fun addData(slist: ArrayList<GetCommentDataList>) {
        this.sList = slist
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = CardViewCommentsChildListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        mContext = parent.context

        return ViewHolder(binding)
    }

//    override fun getItemViewType(position: Int): Int {
//        return position
//    }
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val listPos = sList[position]

        if (listPos.playerImage?.contains("data:image/jpeg;base64," ) == true){

            pic = listPos.playerImage.replace("data:image/jpeg;base64,","")
        }

        if (listPos.isLiked){
            holder.bind.unliked.visibility = View.VISIBLE
        }

        val imageBytes:ByteArray = Base64.decode(pic, Base64.DEFAULT)
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        binding.userPic.setImageBitmap(decodedImage)
//
            holder.bind.tvComment.text = listPos.comment
            holder.bind.tvUserName.text = listPos.playerName
            holder.onBind(listPos,listenerClick, position)

        }


    override fun getItemCount(): Int = sList.size

    class ViewHolder(val bind: CardViewCommentsChildListBinding) : RecyclerView.ViewHolder(bind.root) {
        fun onBind(model: GetCommentDataList, listener: OnItemClick, position: Int) {

            bind.liked.setOnClickListener {
                listener.onLiked(model, position)
                bind.unliked.visibility = View.VISIBLE
            }

            bind.unliked.setOnClickListener {
                listener.onUnLiked(model,position)
                bind.unliked.visibility = View.GONE

            }
            bind.share.setOnClickListener {

                listener.onShare(model, position)
            }


        }
    }



    interface OnItemClick {
        fun onLiked(comment:GetCommentDataList, position: Int)
        fun onUnLiked(comment:GetCommentDataList, position: Int)
        fun onShare(comment: GetCommentDataList,position: Int)


    }
}