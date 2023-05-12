package com.example.aplayer.presenter.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.aplayer.R
import com.example.aplayer.databinding.MusicItemBinding
import com.example.aplayer.domain.music.model.Music

class MainAdapter: RecyclerView.Adapter<MainAdapter.MainViewHolder>() {
    private var onClickListener: OnClickListener? = null
    private var musicList = emptyList<Music>()

    class MainViewHolder(val binding: MusicItemBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

        with(holder.binding) {
            musicItemTitle.text = musicList[position].name
            musicItemDuration.text = musicList[position].duration
            Glide.with(holder.itemView.context)
                .load(musicList[position].artUri)
                .placeholder(R.drawable.im_default)
                .error(R.drawable.im_default)
                .centerCrop()
                .into(musicItemImage)
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, musicList[position] )
            }
        }

    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Music)
    }

    fun setList(list: List<Music>) {
        musicList = list
        notifyDataSetChanged()
    }
}