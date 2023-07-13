package com.example.aplayer.data.player

interface PlayerRepository {

    fun playMusic()

    fun stopMusic()

    fun pauseMusic()

    fun resumeMusic()

    fun seekMusic(progress: Int)

    fun repeatMusic()

    fun shuffleMusic()

    fun skipToNext()

    fun skipToPrevious()

    fun initMediaPlayer()


}