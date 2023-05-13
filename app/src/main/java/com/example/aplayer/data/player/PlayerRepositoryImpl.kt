package com.example.aplayer.data.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.aplayer.domain.music.model.Music
import io.reactivex.Completable
import io.reactivex.Observable

class PlayerRepositoryImpl(
    private val context: Context,
    private var mediaPlayer: MediaPlayer?
    ) : PlayerRepository {

    override fun playMusic(): Observable<Music> {
        return Observable.create { sub ->
            if(mediaPlayer != null) {
                val length = mediaPlayer?.currentPosition
                mediaPlayer?.prepare()
                mediaPlayer?.setOnPreparedListener {
                    if (length != null) {
                        mediaPlayer!!.seekTo(length)
                    }
                    mediaPlayer!!.start()
                }
            }
            sub.onComplete()
        }
    }

    override fun stopMusic(): Observable<Music> {
        return  Observable.create {
            if(mediaPlayer != null) {
                mediaPlayer?.stop()
            }
        }
    }

    override fun seekMusic(progress: Int): Completable {
        return Completable.create {
            mediaPlayer?.seekTo(progress)
            it.onComplete()
        }
    }

    override fun repeatMusic() {
    }

    override fun shuffleMusic() {
    }

    override fun skipMusic(): Observable<Music> {
        return Observable.create {

        }
    }

    override fun previousMusic(): Observable<Music> {
        return Observable.create {

        }
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun initMediaPlayer(music: Music) {
        if(mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, music.uri)
        }
    }

    override fun getMediaPlayer(): MediaPlayer {
        return mediaPlayer!!
    }
}