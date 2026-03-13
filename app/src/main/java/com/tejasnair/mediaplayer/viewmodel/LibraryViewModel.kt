package com.tejasnair.mediaplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.tejasnair.mediaplayer.data.Song

class LibraryViewModel : ViewModel() {
    private val _songs = mutableStateOf(listOf<Song>())
    val songs: State<List<Song>> = _songs

    fun addSong(song: Song) {
        _songs.value += song
    }

    fun clearLibrary() {
        _songs.value = listOf()
    }
}