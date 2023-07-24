package com.example.aplayer.presenter.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.aplayer.data.music.ProviderRepository
import com.example.aplayer.data.music.StorageUtil
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Single

class MainViewModel(
    private val providerRepository: ProviderRepository
) : ViewModel() {
    private val _playingPosition = MutableLiveData(-1)
    val playingPosition: LiveData<Int> = _playingPosition

    fun setPlayingPosition(position: Int) {
        _playingPosition.value = position
    }
    fun getMusic(): Single<List<Music>> {
        return providerRepository.getMusic()
    }

}