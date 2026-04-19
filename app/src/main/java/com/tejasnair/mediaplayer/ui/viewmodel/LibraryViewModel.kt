package com.tejasnair.mediaplayer.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.model.AlbumSummary
import com.tejasnair.mediaplayer.data.model.Playlist
import com.tejasnair.mediaplayer.data.repository.MusicRepository

class LibraryViewModel(private val repository: MusicRepository) : ViewModel() {

    // --- DATA STREAMS ---

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favouriteSongs: StateFlow<List<Song>> = repository.favouriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<AlbumSummary>> = repository.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- SONG ACTIONS ---

    fun getSong(songId: String): Flow<Song?> = repository.getSongById(songId)

    fun toggleFavourite(songId: String) {
        viewModelScope.launch {
            repository.toggleFavourite(songId)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSong(song)
        }
    }


    // --- ALBUM & ARTIST ACTIONS ---

    fun getSongsByAlbum(name: String, artist: String): Flow<List<Song>> =
        repository.getSongsByAlbum(name, artist)
            .map { list ->
                list.sortedWith(
                    compareBy(
                        { it.discNumber }, { it.trackNumber }
                    )
                )
            }

    fun updateAlbumDetails(oldAlbum: String, oldArtist: String, newAlbum: String, newArtist: String, newYear: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlbumDetails(oldAlbum, oldArtist, newAlbum, newArtist, newYear)
        }
    }


    // --- PLAYLIST ACTIONS ---

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(Playlist(playlistName = name))
        }
    }

    fun addSongToPlaylist(songId: String, playlistId: String, position: Int) {
        viewModelScope.launch {
            repository.addSongToPlaylist(songId, playlistId, position)
        }
    }

    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>> =
        repository.getSongsInPlaylist(playlistId)


    // --- LIBRARY MANAGEMENT ---

    var librarySize by mutableLongStateOf(0L)
        private set

    fun loadLibrarySize() {
        viewModelScope.launch(Dispatchers.IO) {
            librarySize = repository.getLibrarySizeBytes()
        }
    }

    fun clearLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearLibrary()
            librarySize = 0L
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