package com.example.aplayer.domain.music.model

data class Music(
    val id: Int ?= null,
    val album: String ?= null,
    val artist: String?= "Неизвестно",
    val data: String?= null,
    val name: String?= null,
    val duration: Long?= null
    )
