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
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.content.ContextCompat
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import androidx.core.net.toUri
import androidx.media3.common.Timeline
import java.util.Collections.emptyList

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private var browserFuture: ListenableFuture<MediaBrowser>? = null
    private val browser: MediaBrowser? get() = if (browserFuture?.isDone == true) browserFuture?.get() else null

    var currentSong by mutableStateOf<Song?>(null)
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)
    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_OFF)
        private set

    var currentQueue: List<Song> by mutableStateOf(emptyList())
        private set

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

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentPosition = 0L

                mediaItem?.let { item ->
                    val metadata = item.mediaMetadata
                    currentSong = Song(
                        filePath = item.mediaId, // This matches your file.absolutePath
                        title = metadata.title?.toString() ?: "Unknown Title",
                        artists = metadata.artist?.toString() ?: "Unknown Artist",
                        album = metadata.albumTitle?.toString() ?: "",
                        albumArtists = metadata.albumArtist?.toString() ?: "",
                        songArtUri = metadata.artworkUri?.toString() ?: "",
                        duration = browser?.duration?.coerceAtLeast(0L) ?: 0L
                    )
                }

                updateQueue()
            }

            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    currentPosition = duration
                    isPlaying = false
                }
                if (state == Player.STATE_READY) {
                    duration = browser?.duration?.coerceAtLeast(0L) ?: 0L
                }
            }

            override fun onRepeatModeChanged(mode: Int) {
                repeatMode = mode
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                updateQueue()
            }
        })
    }

    fun playSong(selectedSong: Song, playlist: List<Song>? = null) {
        currentSong = selectedSong

        browser?.let { player ->
            if (!playlist.isNullOrEmpty()) {
                val mediaItems = playlist.map { it.toMediaItem() }
                val startIndex = playlist.indexOf(selectedSong).coerceAtLeast(0)

                player.setMediaItems(mediaItems, startIndex, 0L)
            } else {
                player.setMediaItem(selectedSong.toMediaItem())
            }

            player.prepare()
            player.play()
        }

        repeatMode = Player.REPEAT_MODE_OFF
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

    fun toggleRepeatMode() {
        val nextMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }

        browser?.repeatMode = nextMode
        repeatMode = nextMode
    }

    fun addToQueue(songToAdd: Song) {

        currentQueue = (currentQueue ?: emptyList()) + songToAdd

        browser?.let { player ->
            val newMediaItem = songToAdd.toMediaItem()

            player.addMediaItem(newMediaItem)

            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
        }
    }

    fun addAlbumToQueue(albumToAdd: List<Song>) {
        for(song in albumToAdd) {
            currentQueue = (currentQueue ?: emptyList()) + song

            browser?.let { player ->
                val newMediaItem = song.toMediaItem()

                player.addMediaItem(newMediaItem)

                if (player.playbackState == Player.STATE_IDLE) {
                    player.prepare()
                }
            }
        }
    }

    fun removeFromQueue(index: Int) {
        val updatedList = currentQueue.toMutableList()
        if (index in updatedList.indices) {
            updatedList.removeAt(index)
            currentQueue = updatedList

            browser?.removeMediaItem(index)
        }
    }

    fun addToNext(song: Song) {
        browser?.let { player ->
            val nextIndex = player.currentMediaItemIndex + 1
            player.addMediaItem(nextIndex, song.toMediaItem())
        }
    }

    fun skipToNext() {
        browser?.seekToNext()
    }

    fun skipToPrevious() {
        browser?.seekToPrevious()
    }

    fun skipToPreviousForce() {
        if (browser?.hasPreviousMediaItem() == true) {
            browser?.seekToPreviousMediaItem()
        } else {
            browser?.seekTo(0L)
        }
    }

    fun playFromQueue(index: Int) {
        browser?.seekTo(index, 0L)
        browser?.play()
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        browser?.let { player ->
            player.moveMediaItem(fromIndex, toIndex)

            val list = currentQueue.toMutableList()
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            currentQueue = list
        }
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

    private fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(this.filePath)
            .setUri(this.filePath.toUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(this.title)
                    .setArtist(this.artists)
                    .setArtworkUri(this.songArtUri?.toUri())
                    .build()
            )
            .build()
    }

    private fun updateQueue() {
        val player = browser ?: return
        val queue = mutableListOf<Song>()

        // Loop through the player's current items and convert them back to Songs
        for (i in 0 until player.mediaItemCount) {
            val item = player.getMediaItemAt(i)
            val metadata = item.mediaMetadata
            queue.add(
                Song(
                    filePath = item.mediaId, // This matches your file.absolutePath
                    title = metadata.title?.toString() ?: "Unknown Title",
                    artists = metadata.artist?.toString() ?: "Unknown Artist",
                    album = metadata.albumTitle?.toString() ?: "",
                    albumArtists = metadata.albumArtist?.toString() ?: "",
                    songArtUri = metadata.artworkUri?.toString() ?: "",
                    duration = browser?.duration?.coerceAtLeast(0L) ?: 0L
                )
            )
        }
        currentQueue = queue
    }

    override fun onCleared() {
        super.onCleared()
        browserFuture?.let {
            MediaBrowser.releaseFuture(it)
        }
    }
}