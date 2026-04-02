package com.tejasnair.mediaplayer.data.local.files

// 1. Android & Core
import android.app.PendingIntent
import android.content.Intent
import android.os.PowerManager

// 2. Media3
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

// 3. Local Project Imports
import com.tejasnair.mediaplayer.MainActivity

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    // This is where you'll link your existing ExoPlayer
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onCreate() {
        super.onCreate()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val player = ExoPlayer.Builder(this).apply {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            setWakeMode(PowerManager.PARTIAL_WAKE_LOCK)
        }.build()

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player

        if (player?.playWhenReady == false || player?.playbackState == Player.STATE_IDLE) {
            stopSelf()
        } else {
            player?.pause()
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }
}