package com.example.aplayer.domain.music.model

data class Music(
    val id: Int ?= null,
    val album: String,
    val artist: String,
    val data: String,
    val name: String,
    val duration: String
    )
