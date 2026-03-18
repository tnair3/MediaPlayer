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
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import com.tejasnair.mediaplayer.data.model.Song
import androidx.core.net.toUri

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Initialize the Player
    private val exoPlayer = ExoPlayer.Builder(application).build().apply {
        // Listener to update 'isPlaying' state automatically
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                this@PlaybackViewModel.isPlaying = isPlayingNow
            }
        })
    }

    // 2. Observable States for the UI
    var currentSong by mutableStateOf<Song?>(null)
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    // 3. Position Tracker (updates the slider every 1 second)
    private var timerJob: Job? = null

    init {
        startProgressUpdater()
    }

    fun playSong(song: Song) {
        currentSong = song

        // Build Metadata for Lockscreen/System
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artists)
            .setArtworkUri(song.songArtUri?.toUri())
            .build()

        // Build the Media Item
        val mediaItem = MediaItem.Builder()
            .setUri(song.filePath.toUri())
            .setMediaId(song.filePath)
            .setMediaMetadata(metadata)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        // Duration is only available after the player is prepared
        duration = exoPlayer.duration.coerceAtLeast(0L)
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        currentPosition = position
    }

    private fun startProgressUpdater() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    currentPosition = exoPlayer.currentPosition
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
                delay(1000) // Update every second
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        exoPlayer.release()
    }
}