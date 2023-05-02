package com.example.aplayer.presenter.main.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.aplayer.R
import com.example.aplayer.databinding.MusicItemBinding
import com.example.aplayer.domain.music.model.Music
import java.sql.Time
import java.util.concurrent.TimeUnit

class MainAdapter: RecyclerView.Adapter<MainAdapter.MainViewHolder>() {
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
            musicItemArtist.text = musicList[position].artist
            musicItemDuration.text = String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(musicList[position].duration!!),
                TimeUnit.MILLISECONDS.toSeconds(musicList[position].duration!!) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(musicList[position].duration!!))
            )
            Glide.with(holder.itemView.context)
                .load(musicList[position].artUri)
                .placeholder(R.drawable.baseline_undefined)
                .error(R.drawable.baseline_undefined)
                .centerCrop()
                .into(musicItemImage)
        }

    }

    fun setList(list: List<Music>) {
        musicList = list
        notifyDataSetChanged()
    }
}