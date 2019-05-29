package com.example.exo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


interface PlayingContract {

    fun play(bean: PlayBean)

    fun currentPlaying(): PlayBean

}


class PlaylistAdapter(
        private val lists: List<PlayBean>,
        private val contract: PlayingContract
) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false))
    }

    override fun getItemCount(): Int = lists.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lists[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val text: TextView = view.findViewById(android.R.id.text1)

        fun bind(bean: PlayBean) {
            if (contract.currentPlaying() == bean) {
                text.setTextColor(Color.WHITE)
                text.setBackgroundColor(Color.BLUE)
            } else {
                text.setTextColor(Color.BLACK)
                text.setBackgroundColor(Color.WHITE)
            }
            text.text = bean.name
            itemView.setOnClickListener {
                contract.play(bean)
            }
        }
    }

}