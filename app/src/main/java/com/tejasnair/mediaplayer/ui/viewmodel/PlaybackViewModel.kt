package com.tejasnair.mediaplayer.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.jvm.java
import com.tejasnair.mediaplayer.data.local.files.PlaybackService
import com.tejasnair.mediaplayer.data.model.Song
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import androidx.core.net.toUri

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private var browserFuture: ListenableFuture<MediaBrowser>? = null
    private val browser: MediaBrowser? get() = if (browserFuture?.isDone == true) browserFuture?.get() else null

    var currentSong by mutableStateOf<Song?>(null)
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    private var timerJob: Job? = null

    init {
        val sessionToken = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        browserFuture = MediaBrowser.Builder(application, sessionToken).buildAsync()

        browserFuture?.addListener({
            setupPlayerListener()
            startProgressUpdater()
        }, ContextCompat.getMainExecutor(application))
    }

    private fun setupPlayerListener() {
        browser?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = browser?.duration?.coerceAtLeast(0L) ?: 0L
                }
            }
        })
    }

    fun playSong(song: Song) {
        currentSong = song

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artists)
            .setArtworkUri(song.songArtUri?.toUri())
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.filePath)
            .setUri(song.filePath.toUri())
            .setMediaMetadata(metadata)
            .build()

        browser?.setMediaItem(mediaItem)
        browser?.prepare()
        browser?.play()
    }

    fun togglePlayPause() {
        if (browser?.isPlaying == true) {
            browser?.pause()
        } else {
            browser?.play()
        }
    }

    fun seekTo(position: Long) {
        browser?.seekTo(position)
        currentPosition = position
    }

    fun stopPlayback() {
        browser?.stop()
        browser?.clearMediaItems()
        currentSong = null
    }

    private fun startProgressUpdater() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                browser?.let {
                    if (it.isPlaying) {
                        currentPosition = it.currentPosition
                        duration = it.duration.coerceAtLeast(0L)
                    }
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        browserFuture?.let {
            MediaBrowser.releaseFuture(it)
        }
    }
}