package com.example.aplayer.domain.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.app.NotificationCompat.*
import androidx.navigation.NavDeepLinkBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.aplayer.MainActivity
import com.example.aplayer.R
import com.example.aplayer.domain.music.model.Music
import com.example.aplayer.utils.PlaybackStatus
import com.example.aplayer.utils.millisecondsToTime

private const val CHANNEL_ID = "media_playback_channel"
private const val CHANNEL_NAME = "Media playback"
private const val CHANNEL_DESCRIPTION = "Media playback controls"

class NotificationHelper(private val context: Context) {

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val contentIntent by lazy { createContentIntent() }

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
            .setSmallIcon(R.drawable.ic_app_logo)
            .addAction(R.drawable.baseline_skip_previous, "previous", playbackAction(3))
            .addAction(notificationAction, "pause", playPauseAction)
            .addAction(R.drawable.baseline_skip_next, "next", playbackAction(2))
            .setOngoing(isPlaying)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
    }

    fun updateNotification(
        playbackStatus: PlaybackStatus,
        activeAudio: Music,
        mediaSession: MediaSessionCompat
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel())
        }

        val notificationBuilder = buildNotification(playbackStatus, mediaSession)
            .setContentText(activeAudio.artist)
            .setContentTitle(activeAudio.name)
            .setContentInfo(activeAudio.duration.millisecondsToTime())
            .setContentIntent(contentIntent)

        val bitmap =  AppCompatResources.getDrawable(context, R.drawable.splash_background)?.toBitmap()
        Glide.with(context)
            .asBitmap()
            .load(activeAudio.artUri)
            .into(object: CustomTarget<Bitmap>() {

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    notificationBuilder.setLargeIcon(resource)
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    notificationBuilder.setLargeIcon(bitmap)
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                    super.onLoadFailed(errorDrawable)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

            })

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
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(context, 0, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun removeNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val NOTIFICATION_ID = 101
    }
}