package com.example.aplayer.domain.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.*
import androidx.navigation.NavDeepLinkBuilder
import com.example.aplayer.MainActivity
import com.example.aplayer.R
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.utils.PlaybackStatus

private const val CHANNEL_ID = "media_playback_channel"
private const val CHANNEL_NAME = "Media playback"
private const val CHANNEL_DESCRIPTION = "Media playback controls"

class NotificationHelper(private val context: Context) {

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() =
        NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

    private fun buildNotification(
        playbackStatus: PlaybackStatus,
        mediaSession: MediaSessionCompat
    ): NotificationCompat.Builder {
        var notificationAction = R.drawable.baseline_pause_circle_filled //needs to be initialized
        var playPauseAction: PendingIntent? = null
        var isPlaying = false

        //Build a new notification according to the current state of the MediaPlayer
        when (playbackStatus) {
            PlaybackStatus.PLAYING -> {
                notificationAction = R.drawable.baseline_pause_circle_filled
                isPlaying = true
                //create the pause action
                playPauseAction = playbackAction(1)
            }
            PlaybackStatus.PAUSED -> {
                notificationAction = R.drawable.baseline_play_circle_filled
                isPlaying = false
                //create the play action
                playPauseAction = playbackAction(0)
            }
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setShowWhen(false)
            .setStyle(
                MediaStyle() // Attach our MediaSession token
                    .setMediaSession(mediaSession.sessionToken) // Show our playback controls in the compact notification view.
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setColor(context.getColor(R.color.black_lite_200))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.baseline_skip_previous, "previous", playbackAction(3))
            .addAction(
                notificationAction, "pause", playPauseAction
            )
            .addAction(
                R.drawable.baseline_skip_next, "next", playbackAction(2)
            )
            .setOngoing(isPlaying)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
    }

    fun updateNotification(
        playbackStatus: PlaybackStatus,
        activeAudio: Music,
        mediaSession: MediaSessionCompat
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel())
        }
        val contentIntent = createContentIntent()

        val notificationBuilder = buildNotification(playbackStatus, mediaSession)
            .setContentText(activeAudio.artist)
            .setContentTitle(activeAudio.name)
            .setContentIntent(contentIntent)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun removeNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(context, PlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
        }
        return null
    }

    private fun createContentIntent(): PendingIntent {
        return NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.tabs_graph)
            .setDestination(R.id.playerFragment)
            .createPendingIntent()
    }

    companion object {
        const val NOTIFICATION_ID = 101
    }
}