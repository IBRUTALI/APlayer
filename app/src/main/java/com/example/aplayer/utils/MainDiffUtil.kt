package com.example.aplayer.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.aplayer.domain.music.model.Music

class MainDiffUtil(
    private val oldList: List<Music>,
    private val newList: List<Music>
): DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].id != newList[newItemPosition].id -> {
                false
            }
            oldList[oldItemPosition].artUri != newList[newItemPosition].artUri -> {
                false
            }
            oldList[oldItemPosition].uri != newList[newItemPosition].uri -> {
                false
            }
            oldList[oldItemPosition].data != newList[newItemPosition].data -> {
                false
            }
            oldList[oldItemPosition].artist != newList[newItemPosition].artist -> {
                false
            }
            oldList[oldItemPosition].size != newList[newItemPosition].size -> {
                false
            }
            oldList[oldItemPosition].name != newList[newItemPosition].name -> {
                false
            }
            oldList[oldItemPosition].duration != newList[newItemPosition].duration -> {
                false
            } else -> true
        }
    }
}