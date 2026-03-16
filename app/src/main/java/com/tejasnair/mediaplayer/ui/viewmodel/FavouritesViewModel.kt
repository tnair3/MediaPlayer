package com.tejasnair.mediaplayer.ui.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.tejasnair.mediaplayer.data.model.Song
import kotlin.collections.set

class FavouritesViewModel : ViewModel() {
    // Core Storage
    private val _songs = mutableStateMapOf<String, Song>()

    // Public read-only access
    val songs: Map<String, Song> get() = _songs

    fun addSong(song: Song) {
        _songs[song.id] = song
    }
}