package com.tejasnair.mediaplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tejasnair.mediaplayer.data.model.*
import com.tejasnair.mediaplayer.data.repository.MusicRepository
import com.tejasnair.mediaplayer.data.local.entities.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow

class LibraryViewModel(private val repository: MusicRepository) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbums: StateFlow<List<Album>> = repository.allAlbums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArtists: StateFlow<List<Artist>> = repository.allArtists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSong(song: Song, artists: List<Artist>, album: Album?) {
        viewModelScope.launch {
            repository.insertFullSongData(song, artists, album)
        }
    }

    fun getSongById(songId: String): Song? {
        return allSongs.value.find { it.id == songId }
    }

    fun getAlbumDetails(id: String): Flow<AlbumDetail> {
        return repository.getAlbumSongs(id)
    }
}