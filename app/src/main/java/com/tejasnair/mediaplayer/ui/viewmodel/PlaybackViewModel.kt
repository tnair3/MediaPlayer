package com.tejasnair.mediaplayer.ui.viewmodel

// 1. Android & Core
import android.app.Application
import android.content.ComponentName
import androidx.compose.animation.core.RepeatMode
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

// 2. Compose Runtime
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// 3. Lifecycle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

// 4. Media3
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken

// 5. Guava / Futures
import com.google.common.util.concurrent.ListenableFuture

// 6. Coroutines
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// 7. Local Project Imports
import com.tejasnair.mediaplayer.data.local.files.PlaybackService
import com.tejasnair.mediaplayer.data.model.Song
import kotlinx.coroutines.newFixedThreadPoolContext

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private var browserFuture: ListenableFuture<MediaBrowser>? = null
    private val browser: MediaBrowser?
        get() = if (browserFuture?.isDone == true) browserFuture?.get() else null

    var currentSongId by mutableStateOf<String?>(null)
        private set

    private var originalQueue: List<String> = emptyList()
    var currentQueue by mutableStateOf<List<String>>(emptyList())
        private set

    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)

    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_OFF)
        private set

    var isShuffleOn by mutableStateOf(false)
        private set

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

    fun playSong(selectedSong: Song, playlist: List<Song>? = null) {
        isShuffleOn = false
        originalQueue = emptyList()
        browser?.shuffleModeEnabled = false

        currentSongId = selectedSong.songId

        browser?.let { player ->
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
        currentSongId = null
        currentQueue = emptyList()
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

    fun incrementSong(incrementVal: Int) {
        val newPosition = currentPosition + incrementVal
        browser?.seekTo(newPosition)
        currentPosition = newPosition
    }
    fun addToQueue(songToAdd: Song) {
        if(currentQueue.isEmpty()) {
            repeatMode = Player.REPEAT_MODE_OFF
            browser?.repeatMode = repeatMode
        }
        currentQueue = currentQueue + songToAdd.songId
        browser?.addMediaItem(songToAdd.toMediaItem())
    }

    fun addAlbumToQueue(albumToAdd: List<Song>) {
        albumToAdd.forEach { song ->
            currentQueue = currentQueue + song.songId
            browser?.addMediaItem(song.toMediaItem())
        }
    }

    fun removeFromQueue(index: Int) {
        if (index in currentQueue.indices) {
            val list = currentQueue.toMutableList()
            list.removeAt(index)
            currentQueue = list
            browser?.removeMediaItem(index)
        }
    }

    fun addToNext(song: Song) {
        if(currentQueue.isEmpty()) {
            repeatMode = Player.REPEAT_MODE_OFF
            browser?.repeatMode = repeatMode
        }

        browser?.let { player ->
            val nextIndex = player.currentMediaItemIndex + 1
            player.addMediaItem(nextIndex, song.toMediaItem())

            val list = currentQueue.toMutableList()
            list.add(nextIndex, song.songId)
            currentQueue = list
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
        browser?.moveMediaItem(fromIndex, toIndex)

        val list = currentQueue.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        currentQueue = list
    }

    private fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        isShuffleOn = shuffleModeEnabled
    }

    fun toggleShuffle() {
        val player = browser ?: return
        val newMode = !isShuffleOn

        player.shuffleModeEnabled = newMode
        isShuffleOn = newMode

        if (newMode) {
            originalQueue = currentQueue.toList()

            val currentIndex = player.currentMediaItemIndex
            val currentId = currentQueue.getOrNull(currentIndex)

            val rest = currentQueue.toMutableList()
            if (currentId != null) rest.remove(currentId)
            rest.shuffle()
            val newQueue = if (currentId != null) listOf(currentId) + rest else rest

            reorderQueueWithMoves(newQueue, player)
            currentQueue = newQueue

        } else {
            if (originalQueue.isNotEmpty()) {
                val currentId = currentQueue.getOrNull(player.currentMediaItemIndex)
                val restoreIndex = if (currentId != null)
                    originalQueue.indexOf(currentId).coerceAtLeast(0) else 0

                reorderQueueWithMoves(originalQueue, player)
                currentQueue = originalQueue.toList()
                originalQueue = emptyList()
            }
        }
    }

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
            .setMediaId(this.songId) // IMPORTANT: use songId
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

        val queue = mutableListOf<String>()
        for (i in 0 until player.mediaItemCount) {
            queue.add(player.getMediaItemAt(i).mediaId)
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