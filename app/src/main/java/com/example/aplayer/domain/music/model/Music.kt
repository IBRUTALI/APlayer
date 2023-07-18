package com.example.aplayer.domain.music.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Music(
    val id: Int? = null,
    val artUri: String? = null,
    val uri: String? = null,
    val data: String? = null,
    val artist: String? = "Неизвестно",
    val size: String? = null,
    val name: String? = null,
    val duration: Long? = null
): Parcelable