package com.example.aplayer.domain.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.aplayer.data.player.PlayerRepository
import com.example.aplayer.domain.music.model.Music
import java.io.IOException


class PlayerService : Service(), PlayerRepository, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {
    private var _mediaPlayer: MediaPlayer? = null
    private val mediaPlayer get() = _mediaPlayer!!
    private var resumePosition = 0
    private var musicList: List<Music> = emptyList()
    private var currentPosition = 0
    private val iBinder = LocalBinder()
    private lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder = iBinder

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            musicList = intent?.extras?.getParcelableArrayList("Music list")?: emptyList()
            currentPosition = intent?.extras?.getInt("Current position", 0)?: 0
        } catch (e: NullPointerException) {
            e.printStackTrace()
            stopSelf()
        }
        //Request audio focus
        if (!requestAudioFocus()) {
            stopSelf()
        }

       initMediaPlayer()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun playMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start();
        }
    }

    override fun stopMusic() {
        if (_mediaPlayer == null) return
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    override fun pauseMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            resumePosition = mediaPlayer.currentPosition
        }
    }

    override fun resumeMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(resumePosition)
            mediaPlayer.start()
        }
    }

    override fun seekMusic(progress: Int) {
        mediaPlayer.seekTo(progress)
    }

    override fun repeatMusic() {
        mediaPlayer.isLooping = !mediaPlayer.isLooping
    }

    override fun shuffleMusic() {
    }

    override fun skipMusic() {

    }

    override fun previousMusic() {

    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun initMediaPlayer() {
        _mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnBufferingUpdateListener(this)
        mediaPlayer.setOnSeekCompleteListener(this)
        mediaPlayer.setOnInfoListener(this)
        mediaPlayer.reset()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            musicList[currentPosition].uri?.let { mediaPlayer.setDataSource(this, it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaPlayer.prepareAsync()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopMusic()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        playMusic()
    }

    override fun onError(mp: MediaPlayer?, error: Int, extra: Int): Boolean {
        when (error) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ->
                Log.d(
                    "MediaPlayer Error",
                    "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
                )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED ->
                Log.d(
                    "MediaPlayer Error",
                    "MEDIA ERROR SERVER DIED $extra"
                )
            MediaPlayer.MEDIA_ERROR_UNKNOWN ->
                Log.d(
                    "MediaPlayer Error",
                    "MEDIA ERROR UNKNOWN $extra"
                )
        }
        return false
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        TODO("Not yet implemented")
    }

    override fun onInfo(mp: MediaPlayer?, p1: Int, p2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (_mediaPlayer == null) initMediaPlayer()
                else if (!mediaPlayer.isPlaying) mediaPlayer.start()
                mediaPlayer.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
                mediaPlayer.release()
                _mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->             // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying) mediaPlayer.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->             // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying) mediaPlayer.setVolume(0.1f, 0.1f)
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result: Int = audioManager.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this)
    }



    override fun onDestroy() {
        super.onDestroy()
        if (_mediaPlayer != null) {
            stopMusic()
            mediaPlayer.release();
        }
        removeAudioFocus();
    }

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }

}

