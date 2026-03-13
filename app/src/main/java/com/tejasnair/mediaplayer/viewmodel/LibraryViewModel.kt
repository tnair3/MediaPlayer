package com.tejasnair.mediaplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import com.tejasnair.mediaplayer.data.Song
import com.tejasnair.mediaplayer.data.Album
import com.tejasnair.mediaplayer.data.Album.Companion.UnknownAlbum
import com.tejasnair.mediaplayer.data.Artist

class LibraryViewModel : ViewModel() {
    private val _songs = mutableStateOf(listOf<Song>())
    val songs: State<List<Song>> = _songs

    val albums: State<Map<Album, List<Song>>> = derivedStateOf {
        _songs.value.groupBy { it.album?: UnknownAlbum }
    }

    val artists: State<Map<Artist, List<Song>>> = derivedStateOf {
        _songs.value.groupBy { it.artist }
    }


    fun addSong(song: Song) {
        _songs.value += song
    }

    fun clearLibrary() {
        _songs.value = listOf()
    }
}