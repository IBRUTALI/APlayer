package com.example.aplayer.domain.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.*
import com.example.aplayer.Broadcast_PLAY_NEW_AUDIO
import com.example.aplayer.R
import com.example.aplayer.data.player.PlayerRepository
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.utils.PlaybackStatus
import java.io.IOException


const val ACTION_PLAY = "com.example.aplayer.ACTION_PLAY"
const val ACTION_PAUSE = "com.example.aplayer.ACTION_PAUSE"
const val ACTION_PREVIOUS = "com.example.aplayer.ACTION_PREVIOUS"
const val ACTION_NEXT = "com.example.aplayer.ACTION_NEXT"
const val ACTION_STOP = "com.example.aplayer.ACTION_STOP"
private const val MEDIA_CHANNEL_ID = "media_playback_channel"

class PlayerService : Service(), PlayerRepository, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {
    private var _mediaPlayer: MediaPlayer? = null
    private val mediaPlayer get() = _mediaPlayer!!
    private var resumePosition = 0
    private var musicList: List<Music> = emptyList()
    private lateinit var activeAudio: Music
    private var currentPosition = -1
    private val iBinder = LocalBinder()
    private lateinit var audioManager: AudioManager
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private lateinit var telephonyManager: TelephonyManager

    //MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var transportControls: MediaControllerCompat.TransportControls? = null

    //AudioPlayer notification ID
    private val NOTIFICATION_ID = 101

    override fun onBind(intent: Intent?): IBinder = iBinder

    override fun onCreate() {
        super.onCreate()
        // Perform one-time setup procedures
        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener()
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver()
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel()
            }
            musicList = intent?.extras?.getParcelableArrayList("Music list") ?: emptyList()
            currentPosition = intent?.extras?.getInt("Current position", -1) ?: -1
            if (currentPosition != -1 && currentPosition < musicList.size) {
                //index is in a valid range
                activeAudio = musicList[currentPosition]
            } else {
                stopSelf();
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            stopSelf()
        }
        //Request audio focus
        if (!requestAudioFocus()) {
            stopSelf()
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession()
                initMediaPlayer()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaSession.release()
        removeNotification()
        return super.onUnbind(intent)
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
        if (_mediaPlayer == null) _mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnBufferingUpdateListener(this)
        mediaPlayer.setOnSeekCompleteListener(this)
        mediaPlayer.setOnInfoListener(this)
        mediaPlayer.reset()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            activeAudio.uri?.let { mediaPlayer.setDataSource(this, it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaPlayer.prepareAsync()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        //Invoked when playback of a media source has completed.
        stopMusic()
        removeNotification()
        //stop the service
        stopSelf()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        //Invoked when the media source is ready for playback.
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
        //Invoked indicating the completion of a seek operation.
    }

    override fun onInfo(mp: MediaPlayer?, p1: Int, p2: Int): Boolean {
        //Invoked to communicate some info
        return false
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, p1: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
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

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMusic()
            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            @Deprecated("Deprecated in Java")
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (_mediaPlayer != null) {
                        pauseMusic()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->                   // Phone idle. Start playing.
                        if (_mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeMusic()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }

    private val playNewAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            currentPosition = intent.extras?.getInt("Current position", 0) ?: 0
            if (currentPosition != -1 && currentPosition < musicList.size) {
                //index is in a valid range
                musicList = intent.extras?.getParcelableArrayList("Music list") ?: emptyList()
            } else {
                stopSelf()
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMusic()
            mediaPlayer.reset()
            initMediaPlayer()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    private fun register_playNewAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Broadcast_PLAY_NEW_AUDIO)
        registerReceiver(playNewAudio, filter)
    }

    private fun createChannel() {
        // The id of the channel.
        val id = MEDIA_CHANNEL_ID
        // The user-visible name of the channel.
        val name: CharSequence = "Media playback"
        // The user-visible description of the channel.
        val description = "Media playback controls"
        val importance: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            0
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(id, name, importance)
            // Configure the notification channel.
            mChannel.description = description
            mChannel.setShowBadge(false)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                mChannel
            )
        }
    }

    @Throws(RemoteException::class)
    private fun initMediaSession() {
        if (mediaSessionManager != null) return  //mediaSessionManager exists
        mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        //Get MediaSessions transport controls
        transportControls = mediaSession.controller.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession.isActive = true
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        //Set mediaSession's MetaData
        updateMetaData()

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                super.onPlay()
                resumeMusic()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                super.onPause()
                pauseMusic()
                buildNotification(PlaybackStatus.PAUSED)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                skipToNext()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                skipToPrevious()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                super.onStop()
                removeNotification()
                //Stop the service
                stopSelf()
            }

            override fun onSeekTo(position: Long) {
                super.onSeekTo(position)
            }
        })
    }

    private fun updateMetaData() {
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    activeAudio.artist
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    activeAudio.name
                )
                .build()
        )
    }

    private fun skipToNext() {
        if (currentPosition == musicList.size - 1) {
            //if last in playlist
            currentPosition = 0
            activeAudio = musicList[currentPosition]
        } else {
            //get next in playlist
            activeAudio = musicList[++currentPosition]
        }

        //Update stored index
        stopMusic()
        //reset mediaPlayer
        mediaPlayer.reset()
        initMediaPlayer()
    }

    private fun skipToPrevious() {
        if (currentPosition == 0) {
            //if first in playlist
            //set index to the last of audioList
            currentPosition = musicList.size - 1
            activeAudio = musicList[currentPosition]
        } else {
            //get previous in playlist
            activeAudio = musicList[--currentPosition]
        }

        //Update stored index
        stopMusic()
        //reset mediaPlayer
        mediaPlayer.reset()
        initMediaPlayer()
    }

    private fun buildNotification(playbackStatus: PlaybackStatus) {
        var notificationAction = R.drawable.baseline_pause_circle_filled //needs to be initialized
        var play_pauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        when (playbackStatus) {
            PlaybackStatus.PLAYING -> {
                notificationAction = R.drawable.baseline_pause_circle_filled
                //create the pause action
                play_pauseAction = playbackAction(1)
            }
            PlaybackStatus.PAUSED -> {
                notificationAction = R.drawable.baseline_play_circle_filled
                //create the play action
                play_pauseAction = playbackAction(0)
            }
        }
        // Create a new Notification
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, MEDIA_CHANNEL_ID)
                .setShowWhen(false) // Set the Notification style
                .setStyle(
                    MediaStyle() // Attach our MediaSession token
                        .setMediaSession(mediaSession.sessionToken) // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2)
                )
                // Set the Notification color
                .setColor(resources.getColor(R.color.black_lite_200))
                // Set the large and small icons
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                // Set Notification content information
                .setContentText(activeAudio.artist)
                .setContentTitle(activeAudio.name)
                // Add playback actions
                .addAction(R.drawable.baseline_skip_previous, "previous", playbackAction(3))
                .addAction(
                    notificationAction, "pause", play_pauseAction
                )
                .addAction(
                    R.drawable.baseline_skip_next, "next", playbackAction(2)
                )
                .setSound(null)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, PlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            else -> {}
        }
        return null
    }

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        val actionString = playbackAction.action
        if (actionString.equals(ACTION_PLAY, ignoreCase = true)) {
            transportControls?.play()
        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
            transportControls?.pause()
        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
            transportControls?.skipToNext()
        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
            transportControls?.skipToPrevious()
        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
            transportControls?.stop()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (_mediaPlayer != null) {
            stopMusic()
            mediaPlayer.release()
        }
        removeAudioFocus()
        removeNotification()

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver)
        unregisterReceiver(playNewAudio)
    }

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }

}

