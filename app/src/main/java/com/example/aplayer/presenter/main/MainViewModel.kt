package com.example.aplayer.presenter.main

import androidx.lifecycle.ViewModel
import com.example.aplayer.data.music.ProviderRepository
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Single

class MainViewModel(
    private val providerRepository: ProviderRepository
) : ViewModel() {

    fun getMusic(): Single<List<Music>> {
        return providerRepository.getMusic()
    }
}