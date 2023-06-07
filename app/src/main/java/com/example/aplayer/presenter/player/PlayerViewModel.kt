package com.example.aplayer.presenter.player

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.aplayer.domain.player.PlayerRepositoryImpl
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Completable
import io.reactivex.Observable

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private var mediaPlayer: MediaPlayer? = null
    var audioPosition = MutableLiveData<Int>()

    private val playerRepository = PlayerRepositoryImpl(application.applicationContext, mediaPlayer)
    fun initMediaPlayer(music: Music) {
        playerRepository.initMediaPlayer(music)
    }

    fun playMusic(): Observable<Music> {
        return playerRepository.playMusic()
    }

    fun stopMusic(): Observable<Music> {
        return playerRepository.stopMusic()
    }


    fun musicIsPlaying(): Boolean {
        return playerRepository.isPlaying()
    }

    fun seekMusic(progress: Int): Completable {
        return playerRepository.seekMusic(progress)
    }

    fun getMediaPlayer(): MediaPlayer {
        return playerRepository.getMediaPlayer()
    }

    fun closePlayer() {
        mediaPlayer = null
    }

    fun skipMusic() {
        audioPosition.value = audioPosition.value?.plus(1)
    }

    fun previousMusic() {
        audioPosition.value = audioPosition.value?.minus(1)
    }

    fun repeatMusic() {
        playerRepository.repeatMusic()
    }

    fun shuffleMusic() {
        playerRepository.shuffleMusic()
    }

}