package com.example.aplayer.presenter.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.aplayer.R
import com.example.aplayer.data.music.StorageUtil
import com.example.aplayer.databinding.MusicItemBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.utils.MainDiffUtil
import kotlin.properties.Delegates

class MainAdapter: RecyclerView.Adapter<MainAdapter.MainViewHolder>() {
    private var onClickListener: OnClickListener? = null
    private var musicList = emptyList<Music>()
    private var playingPosition = -1
    private var isPlayingPosition = false

    class MainViewHolder(val binding: MusicItemBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val storageUtil = StorageUtil(parent.context)
        playingPosition = storageUtil.loadAudioIndex()
        isPlayingPosition = storageUtil.isPlayingPosition()
        return MainViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        with(holder.binding) {
            musicItemTitle.text = musicList[position].name
            musicItemDuration.text = musicList[position].duration
            if(playingPosition == position && isPlayingPosition) {
                musicItemPlay.setImageResource(R.drawable.baseline_pause_24)
            } else musicItemPlay.setImageResource(R.drawable.baseline_play_arrow_24)
            Glide.with(holder.itemView.context)
                .load(musicList[position].artUri)
                .placeholder(R.drawable.im_default)
                .error(R.drawable.im_default)
                .centerCrop()
                .into(musicItemImage)
        }

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener?.onClick(position, musicList as ArrayList<Music>)
            }
        }

    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, list: ArrayList<Music>)
    }

    fun setList(newList: List<Music>) {
        val diffUtil = MainDiffUtil(musicList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        diffResult.dispatchUpdatesTo(this)
        musicList = newList
    }

    fun updateList(position: Int, isPlaying: Boolean) {
        playingPosition = position
        isPlayingPosition = isPlaying
        notifyItemChanged(position)
        when(position) {
            0 -> {
                notifyItemChanged(musicList.size-1, Object())
                notifyItemChanged(position+1, Object())
            }
            musicList.size-1-> {
                notifyItemChanged(position-1, Object())
                notifyItemChanged(0, Object())
            }
            else -> {
                notifyItemChanged(position-1, Object())
                notifyItemChanged(position+1, Object())
            }
        }

    }

}