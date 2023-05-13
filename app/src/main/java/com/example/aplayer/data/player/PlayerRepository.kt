package com.example.aplayer.data.player

import android.media.MediaPlayer
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Completable
import io.reactivex.Observable

interface PlayerRepository {

    fun playMusic(): Observable<Music>

    fun stopMusic(): Observable<Music>

    fun seekMusic(progress: Int): Completable

    fun repeatMusic()

    fun shuffleMusic()

    fun skipMusic(): Observable<Music>

    fun previousMusic(): Observable<Music>

    fun isPlaying(): Boolean

    fun initMediaPlayer(music: Music)

    fun getMediaPlayer(): MediaPlayer

}