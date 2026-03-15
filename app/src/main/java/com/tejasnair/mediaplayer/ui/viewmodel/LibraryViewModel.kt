package com.tejasnair.mediaplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.derivedStateOf
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.model.Album
import com.tejasnair.mediaplayer.data.model.Artist

class LibraryViewModel : ViewModel() {
    // Core Storage
    private val _songs = mutableStateMapOf<String, Song>()
    private val _albums = mutableStateMapOf<String, Album>()
    private val _artists = mutableStateMapOf<String, Artist>()

    // Public read-only access
    val songs: Map<String, Song> get() = _songs
    val albums: Map<String, Album> get() = _albums
    val artists: Map<String, Artist> get() = _artists

    // Derived Storage
    val albumsToSongs: State<Map<String, List<String>>> = derivedStateOf {
        _songs.values.groupBy { it.album?.id ?: Album.UnknownAlbum.id }
            .mapValues { it.value.map { song -> song.id } }
    }

    val artistsToSongs: State<Map<String, List<String>>> = derivedStateOf {
        _songs.values.groupBy { it.artist.id }
            .mapValues { it.value.map { song -> song.id } }
    }

    fun addSong(song: Song) {
        _songs[song.id] = song

        song.album?.let { album ->
            if (!_albums.containsKey(album.id)) {
                _albums[album.id] = album
            }
        }

        if (!_artists.containsKey(song.artist.id)) {
            _artists[song.artist.id] = song.artist
        }
    }
}