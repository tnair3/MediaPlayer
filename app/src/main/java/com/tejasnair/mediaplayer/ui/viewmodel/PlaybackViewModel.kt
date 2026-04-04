package com.tejasnair.mediaplayer.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.tejasnair.mediaplayer.data.local.files.PlaybackService
import com.tejasnair.mediaplayer.data.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    // Browser Access

    private var browserFuture: ListenableFuture<MediaBrowser>? = null

    // Unwraps the future only when it's done — null safe throughout
    private val browser: MediaBrowser?
        get() = if (browserFuture?.isDone == true) browserFuture?.get() else null

    // Playback State

    var currentSongId by mutableStateOf<String?>(null)
        private set

    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_OFF)
        private set

    var isShuffleOn by mutableStateOf(false)
        private set

    // Queue State

    var currentQueue by mutableStateOf<List<String>>(emptyList())
        private set

    // Preserved when shuffle is enabled so we can restore it when turned off
    private var originalQueue: List<String> = emptyList()

    // Init

    private var timerJob: Job? = null

    init {
        val sessionToken = SessionToken(
            application,
            ComponentName(application, PlaybackService::class.java)
        )
        browserFuture = MediaBrowser.Builder(application, sessionToken).buildAsync()
        browserFuture?.addListener({
            setupPlayerListener()
            startProgressUpdater()
        }, ContextCompat.getMainExecutor(application))
    }

    // Player Listener

    private fun setupPlayerListener() {
        browser?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentPosition = 0L
                currentSongId = mediaItem?.mediaId
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

    // Core Playback Controls

    fun playSong(selectedSong: Song, playlist: List<Song>? = null) {
        // Always reset shuffle when starting a new queue
        isShuffleOn = false
        originalQueue = emptyList()
        browser?.shuffleModeEnabled = false

        currentSongId = selectedSong.songId

        val player = browser ?: return
        if (!playlist.isNullOrEmpty()) {
            val mediaItems = playlist.map { it.toMediaItem() }
            val startIndex = playlist.indexOf(selectedSong).coerceAtLeast(0)
            player.setMediaItems(mediaItems, startIndex, 0L)
        } else {
            player.setMediaItem(selectedSong.toMediaItem())
        }

        repeatMode = Player.REPEAT_MODE_OFF
        player.repeatMode = repeatMode
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        val player = browser ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(position: Long) {
        browser?.seekTo(position)
        currentPosition = position
    }

    fun incrementSong(incrementVal: Int) {
        val newPosition = currentPosition + incrementVal
        browser?.seekTo(newPosition)
        currentPosition = newPosition
    }

    fun stopPlayback() {
        browser?.stop()
        browser?.clearMediaItems()
        currentSongId = null
        currentQueue = emptyList()
    }

    // Navigation

    fun skipToNext() {
        browser?.seekToNext()
    }

    fun skipToPrevious() {
        browser?.seekToPrevious()
    }

    fun skipToPreviousForce() {
        val player = browser ?: return
        if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
        else player.seekTo(0L)
    }

    fun playFromQueue(index: Int) {
        browser?.seekTo(index, 0L)
        browser?.play()
    }

    // Repeat & Shuffle

    fun toggleRepeatMode() {
        val nextMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        browser?.repeatMode = nextMode
        repeatMode = nextMode
    }

    fun toggleShuffle() {
        val player = browser ?: return
        val newMode = !isShuffleOn

        player.shuffleModeEnabled = newMode
        isShuffleOn = newMode

        if (newMode) {
            // Save the current order before shuffling
            originalQueue = currentQueue.toList()

            val currentId = currentQueue.getOrNull(player.currentMediaItemIndex)
            val rest = currentQueue.toMutableList().also { if (currentId != null) it.remove(currentId) }
            rest.shuffle()

            val newQueue = if (currentId != null) listOf(currentId) + rest else rest
            reorderQueueWithMoves(newQueue, player)
            currentQueue = newQueue

        } else {
            if (originalQueue.isNotEmpty()) {
                val currentId = currentQueue.getOrNull(player.currentMediaItemIndex)
                val restoreIndex = currentId?.let { originalQueue.indexOf(it).coerceAtLeast(0) } ?: 0

                reorderQueueWithMoves(originalQueue, player)
                currentQueue = originalQueue.toList()
                originalQueue = emptyList()
            }
        }
    }

    // Queue Management

    fun addToQueue(songToAdd: Song) {
        if (currentQueue.isEmpty()) {
            repeatMode = Player.REPEAT_MODE_OFF
            browser?.repeatMode = repeatMode
        }
        currentQueue = currentQueue + songToAdd.songId
        browser?.addMediaItem(songToAdd.toMediaItem())
    }

    fun addAlbumToQueue(songs: List<Song>) {
        songs.forEach { song ->
            currentQueue = currentQueue + song.songId
            browser?.addMediaItem(song.toMediaItem())
        }
    }

    fun addToNext(song: Song) {
        val player = browser ?: return
        if (currentQueue.isEmpty()) {
            repeatMode = Player.REPEAT_MODE_OFF
            player.repeatMode = repeatMode
        }

        val nextIndex = player.currentMediaItemIndex + 1
        player.addMediaItem(nextIndex, song.toMediaItem())

        val list = currentQueue.toMutableList()
        list.add(nextIndex, song.songId)
        currentQueue = list
    }

    fun removeFromQueue(index: Int) {
        if (index !in currentQueue.indices) return
        val list = currentQueue.toMutableList()
        list.removeAt(index)
        currentQueue = list
        browser?.removeMediaItem(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        browser?.moveMediaItem(fromIndex, toIndex)
        val list = currentQueue.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        currentQueue = list
    }

    // Internal Helpers

    private fun reorderQueueWithMoves(targetOrder: List<String>, player: MediaBrowser) {
        val currentOrder = (0 until player.mediaItemCount)
            .map { player.getMediaItemAt(it).mediaId }
            .toMutableList()

        for (targetIndex in targetOrder.indices) {
            val id = targetOrder[targetIndex]
            val currentIndex = currentOrder.indexOf(id)
            if (currentIndex == targetIndex) continue
            player.moveMediaItem(currentIndex, targetIndex)
            currentOrder.removeAt(currentIndex)
            currentOrder.add(targetIndex, id)
        }
    }

    private fun updateQueue() {
        val player = browser ?: return
        currentQueue = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId }
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
            .setMediaId(this.songId)
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

    override fun onCleared() {
        super.onCleared()
        browserFuture?.let { MediaBrowser.releaseFuture(it) }
    }
}