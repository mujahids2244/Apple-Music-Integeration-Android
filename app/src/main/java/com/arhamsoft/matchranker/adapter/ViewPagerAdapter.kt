package com.arhamsoft.matchranker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arhamsoft.matchranker.R

class ViewPagerAdapter(private var title: List<String>, private var detail: List<String>,private var img: List<Int>) :
    RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val itemTitle: TextView = itemView.findViewById(R.id.tv1)
        val itemDetail: TextView = itemView.findViewById(R.id.tv2)
        val itemImg: ImageView = itemView.findViewById(R.id.mainlogo)
    }


    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): Pager2ViewHolder {
    return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_item_page, parent,false))
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        holder.itemTitle.text = title[position]
        holder.itemDetail.text = detail[position]
        holder.itemImg.setImageResource(img[position])
    }

    override fun getItemCount(): Int {
        return title.size

    }
}