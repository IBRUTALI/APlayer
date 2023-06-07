package com.example.aplayer

import android.content.Context
import com.example.aplayer.data.music.ProviderRepository
import com.example.aplayer.domain.music.ProviderRepositoryImpl

object Repositories {

    private lateinit var applicationContext: Context

    val providerRepository: ProviderRepository by lazy {
        ProviderRepositoryImpl(applicationContext)
    }

    fun init(context: Context) {
        applicationContext = context
    }

}