package com.focal.app.feature_listen

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.focal.app.MainActivity

/**
 * 听书与播客后台常驻播放 Service
 * 保证切出 App / 锁屏后音频继续播放，并在通知栏提供纯图标控制胶囊
 */
class FocalAudioService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        player?.let { p ->
            mediaSession = MediaSession.Builder(this, p)
                .setSessionActivity(sessionActivityPendingIntent)
                .build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
