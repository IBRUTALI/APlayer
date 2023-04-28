package com.example.aplayer.presenter.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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
            musicItemId.text = "${position + 1}"
            musicItemTitle.text = musicList[position].name
            musicItemArtist.text = musicList[position].artist
            musicItemDuration.text = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(musicList[position].duration!!),
                TimeUnit.MILLISECONDS.toSeconds(musicList[position].duration!!) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(musicList[position].duration!!))
            )
        }
    }

    fun setList(list: List<Music>) {
        musicList = list
        notifyDataSetChanged()
    }
}