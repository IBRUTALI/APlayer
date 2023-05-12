package com.example.aplayer.data.player

interface PlayerRepository {

    fun playMusic()

    fun stopMusic()

    fun seekMusic()

    fun repeatMusic()

    fun shuffleMusic()

    fun likeMusic()

    fun dislikeMusic()

    fun skipMusic()

    fun previousMusic()
}