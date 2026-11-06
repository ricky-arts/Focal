package com.focal.app.feature_renderer

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView

@Composable
fun NativeDashPlayerView(
    videoTrackUrl: String,
    audioTrackUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember(videoTrackUrl, audioTrackUrl) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            val mediaSource = createDashOrMergedSource(context, videoTrackUrl, audioTrackUrl)
            setMediaSource(mediaSource)
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
    )
}

private fun createDashOrMergedSource(
    context: Context,
    videoUrl: String,
    audioUrl: String?
): MediaSource {
    val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setDefaultRequestProperties(mapOf("Referer" to "https://www.bilibili.com/"))
        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/126.0.0.0 Safari/537.36")

    val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(videoUrl))

    if (audioUrl.isNullOrBlank()) {
        return videoSource
    }

    val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(audioUrl))

    return MergingMediaSource(videoSource, audioSource)
}
