package com.tejasnair.mediaplayer.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.tejasnair.mediaplayer.data.model.AlbumSummary
import com.tejasnair.mediaplayer.data.model.Playlist
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.model.Vinyl
import com.tejasnair.mediaplayer.data.model.VinylSide
import com.tejasnair.mediaplayer.data.model.FullVinylRecord
import com.tejasnair.mediaplayer.data.repository.MusicRepository

class LibraryViewModel(
    private val repository: MusicRepository,
    application: android.app.Application
) : AndroidViewModel(application) {

    // --- DATA STREAMS ---

    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favouriteSongs: StateFlow<List<Song>> = repository.favouriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<AlbumSummary>> = repository.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVinyls: StateFlow<List<FullVinylRecord>> = repository.allVinyls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SONG ACTIONS ---

    fun getSong(songId: String): Flow<Song?> = repository.getSongById(songId)

    fun toggleFavourite(songId: String) {
        viewModelScope.launch { repository.toggleFavourite(songId) }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteSong(song) }
    }

    // --- ALBUMS ---

    fun getSongsByAlbum(name: String, artist: String): Flow<List<Song>> =
        repository.getSongsByAlbum(name, artist)
            .map { list -> list.sortedWith(compareBy({ it.discNumber }, { it.trackNumber })) }

    fun updateAlbumDetails(oldAlbum: String, oldArtist: String, newAlbum: String, newArtist: String, newYear: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAlbumDetails(oldAlbum, oldArtist, newAlbum, newArtist, newYear)
        }
    }

    // --- PLAYLISTS ---

    fun createPlaylist(name: String) {
        viewModelScope.launch { repository.createPlaylist(Playlist(playlistName = name)) }
    }

    fun createPlaylistWithId(playlist: Playlist) {
        viewModelScope.launch { repository.createPlaylist(playlist) }
    }

    fun updatePlaylistName(playlistId: String, newName: String) {
        viewModelScope.launch { repository.updatePlaylistName(playlistId, newName) }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch { repository.deletePlaylist(playlistId) }
    }

    fun addSongToPlaylist(songId: String, playlistId: String, position: Int) {
        viewModelScope.launch { repository.addSongToPlaylist(songId, playlistId, position) }
    }

    fun addSongsToPlaylist(songIds: List<String>, playlistId: String, startPosition: Int) {
        viewModelScope.launch { repository.addSongsToPlaylist(songIds, playlistId, startPosition) }
    }

    fun removeSongFromPlaylist(songId: String, playlistId: String) {
        viewModelScope.launch { repository.removeSongFromPlaylist(songId, playlistId) }
    }

    fun reorderPlaylist(playlistId: String, orderedSongIds: List<String>) {
        viewModelScope.launch { repository.reorderPlaylist(playlistId, orderedSongIds) }
    }

    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>> =
        repository.getSongsInPlaylist(playlistId)

    fun getPlaylistSongCount(playlistId: String): Flow<Int> =
        repository.getPlaylistSongCount(playlistId)

    fun copyImageToInternalStorage(uri: android.net.Uri, playlistId: String): String? {
        return repository.copyImageToInternalStorage(
            getApplication<android.app.Application>().applicationContext,
            uri,
            playlistId
        )
    }

    // --- VINYLS ---

    fun getFullVinylRecordById(vinylId: String): Flow<FullVinylRecord?> =
        repository.getFullVinylRecordById(vinylId)

    suspend fun createVinyl(vinyl: Vinyl) {
        repository.createVinyl(vinyl)
    }

    suspend fun createVinylSide(side: VinylSide) {
        repository.createVinylSide(side)
    }

    fun deleteVinyl(vinylId: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteVinyl(vinylId) }
    }

    fun updateVinylDetails(vinylId: String, title: String, artist: String?) {
        viewModelScope.launch(Dispatchers.IO) { repository.updateVinylDetails(vinylId, title, artist) }
    }

    fun updateVinylCoverArt(vinylId: String, coverArtUri: String?) {
        viewModelScope.launch(Dispatchers.IO) { repository.updateVinylCoverArt(vinylId, coverArtUri) }
    }

    fun updateVinylSideName(vinylSideId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.updateVinylSideName(vinylSideId, newName) }
    }

    fun deleteVinylSide(vinylSideId: String) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteVinylSide(vinylSideId) }
    }

    fun addSongToVinylSide(songId: String, vinylSideId: String, trackPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongToVinylSide(songId, vinylSideId, trackPosition)
        }
    }

    fun addSongsToVinylSide(songIds: List<String>, vinylSideId: String, startPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSongsToVinylSide(songIds, vinylSideId, startPosition)
        }
    }

    fun removeSongFromVinylSide(vinylSideId: String, songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeSongFromVinylSide(vinylSideId, songId)
        }
    }

    fun reorderVinylSide(vinylSideId: String, orderedSongIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reorderVinylSide(vinylSideId, orderedSongIds)
        }
    }

    fun getSongsInVinylSide(vinylSideId: String): Flow<List<Song>> =
        repository.getSongsInVinylSide(vinylSideId)

    fun copyVinylImageToInternalStorage(context: android.content.Context, uri: android.net.Uri, vinylId: String): String? {
        return repository.copyVinylImageToInternalStorage(context, uri, vinylId)
    }

    // --- LIBRARY MANAGEMENT ---

    var librarySize by mutableLongStateOf(0L)
        private set

    fun loadLibrarySize() {
        viewModelScope.launch(Dispatchers.IO) { librarySize = repository.getLibrarySizeBytes() }
    }

    fun clearLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearLibrary()
            librarySize = 0L
        }
    }
}

class LibraryViewModelFactory(
    private val repository: MusicRepository,
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}