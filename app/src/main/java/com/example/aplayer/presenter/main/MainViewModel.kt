package com.example.aplayer.presenter.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.aplayer.data.music.ProviderRepositoryImpl
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Single

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val providerRepositoryImpl = ProviderRepositoryImpl(application.applicationContext)

    fun getMusic(): Single<List<Music>> {
        return providerRepositoryImpl.getMusic()
    }
}