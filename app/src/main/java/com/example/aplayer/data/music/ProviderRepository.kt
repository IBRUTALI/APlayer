package com.example.aplayer.data.music

import com.example.aplayer.domain.music.model.Music
import io.reactivex.Single

interface ProviderRepository {
    fun getMusic(): Single<List<Music>>
}