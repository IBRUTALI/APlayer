package com.example.aplayer.domain.music.model

import android.graphics.Bitmap
import android.net.Uri

data class Music(
    val id: Int ?= null,
    val artUri: Uri ?= null,
    val data: String ?= null,
    val artist: String?= "Неизвестно",
    val size: String?= null,
    val name: String?= null,
    val duration: Long?= null
    )
