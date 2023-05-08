package com.example.aplayer.domain.music.model

import android.net.Uri
import java.io.Serializable

data class Music(
    val id: Int ?= null,
    val artUri: Uri ?= null,
    val uri: Uri ?= null,
    val data: String ?= null,
    val artist: String?= "Неизвестно",
    val size: String?= null,
    val name: String?= null,
    val duration: String?= null
    ) : Serializable
