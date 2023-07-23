package com.example.aplayer.presenter.main.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.aplayer.R
import com.example.aplayer.data.music.StorageUtil
import com.example.aplayer.databinding.MusicItemBinding
import com.example.aplayer.databinding.MusicItemGridBinding
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.presenter.main.adapter.AdapterState.*
import com.example.aplayer.utils.MainDiffUtil
import com.example.aplayer.utils.millisecondsToTime
import kotlin.properties.Delegates

class MainAdapter(
   private val state: AdapterState
) : RecyclerView.Adapter<ViewHolder>() {
    private var onClickListener: OnClickListener? = null
    private var musicList = emptyList<Music>()
    private var playingPosition = -1
    private var isPlayingPosition = false

    class MainViewHolder(val binding: MusicItemBinding) : ViewHolder(binding.root)
    class MainGridViewHolder(val binding: MusicItemGridBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val storageUtil = StorageUtil(parent.context)
        playingPosition = storageUtil.loadAudioIndex()
        isPlayingPosition = storageUtil.isPlayingPosition()
        return when (state) {
            LINEAR -> {
                val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MainViewHolder(binding)
            }
            GRID -> {
                val binding = MusicItemGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MainGridViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(state) {
            LINEAR -> {
                val viewHolder = holder as MainViewHolder
                with(viewHolder.binding) {
                    musicItemTitle.text = musicList[position].name
                    musicItemDuration.text = musicList[position].duration.millisecondsToTime()
                    musicItemArtist.text = musicList[position].artist
                    viewHolder.itemView.isSelected = playingPosition == position
                    if (playingPosition == position && isPlayingPosition) {
                        musicItemPlay.setImageResource(R.drawable.baseline_pause_24)
                    } else musicItemPlay.setImageResource(R.drawable.baseline_play_arrow_24)
                    Glide.with(holder.itemView.context)
                        .load(musicList[position].artUri)
                        .placeholder(R.drawable.item_background)
                        .error(R.drawable.item_background)
                        .centerCrop()
                        .into(musicItemImage)
                }
            }

            GRID -> {
                val viewHolder = holder as MainGridViewHolder
                with(viewHolder.binding) {
                    musicItemTitle.text = musicList[position].name
                    musicItemDuration.text = musicList[position].duration.millisecondsToTime()
                    viewHolder.itemView.isSelected = playingPosition == position
                    if (playingPosition == position && isPlayingPosition) {
                        musicItemPlay.setImageResource(R.drawable.baseline_pause_24)
                    } else musicItemPlay.setImageResource(R.drawable.baseline_play_arrow_24)
                    Glide.with(holder.itemView.context)
                        .load(musicList[position].artUri)
                        .placeholder(R.drawable.item_background)
                        .error(R.drawable.item_background)
                        .centerCrop()
                        .into(musicItemImage)
                }
            }
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

    override fun getItemViewType(position: Int): Int {
        return position % 2 * 2
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
        when (position) {
            0 -> {
                notifyItemChanged(musicList.size - 1, Object())
                notifyItemChanged(position + 1, Object())
            }
            musicList.size - 1 -> {
                notifyItemChanged(position - 1, Object())
                notifyItemChanged(0, Object())
            }
            else -> {
                notifyItemChanged(position - 1, Object())
                notifyItemChanged(position + 1, Object())
            }
        }

    }

}