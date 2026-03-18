package com.tejasnair.mediaplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import com.tejasnair.mediaplayer.data.repository.MusicRepository
import com.tejasnair.mediaplayer.data.model.*
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.io.File
import android.util.Log

class LibraryViewModel(private val repository: MusicRepository) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<AlbumSummary>> = repository.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<String>> = repository.artists
        .map { rawArtistList ->
            rawArtistList.flatMap { fullString ->
                fullString.split(Regex(",|&|\\band\\b", RegexOption.IGNORE_CASE))
                    .map { it.trim() }
            }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSongsByArtist(artistName: String): Flow<List<Song>> {
        return allSongs.map { songs ->
            songs.filter { it.artists.contains(artistName, ignoreCase = true) }
        }
    }

    fun getSongsByAlbum(name: String, artist: String): Flow<List<Song>> {
        return allSongs.map { list ->
            list.filter { it.album == name && it.albumArtists == artist }
                .sortedWith(
                    compareBy<Song> { it.discNumber }
                        .thenBy { it.trackNumber }
                )
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSong(song)
        }
    }
}

class LibraryViewModelFactory(private val repository: MusicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}