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
import com.tejasnair.mediaplayer.data.repository.PlaylistRepository
import com.tejasnair.mediaplayer.data.repository.SongRepository
import com.tejasnair.mediaplayer.data.repository.VinylRepository

class LibraryViewModel(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val vinylRepository: VinylRepository,
    application: android.app.Application
) : AndroidViewModel(application) {

    // --- DATA STREAMS ---

    val allSongs: StateFlow<List<Song>> = songRepository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favouriteSongs: StateFlow<List<Song>> = songRepository.favouriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<AlbumSummary>> = songRepository.albums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = playlistRepository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVinyls: StateFlow<List<FullVinylRecord>> = vinylRepository.allVinyls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SONG ACTIONS ---

    fun getSong(songId: String): Flow<Song?> = songRepository.getSongById(songId)

    fun toggleFavourite(songId: String) {
        viewModelScope.launch { songRepository.toggleFavourite(songId) }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) { songRepository.deleteSong(song) }
    }

    // --- ALBUMS ---

    fun getSongsByAlbum(name: String, artist: String): Flow<List<Song>> =
        songRepository.getSongsByAlbum(name, artist)
            .map { list -> list.sortedWith(compareBy({ it.discNumber }, { it.trackNumber })) }

    fun updateAlbumDetails(oldAlbum: String, oldArtist: String, newAlbum: String, newArtist: String, newYear: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            songRepository.updateAlbumDetails(oldAlbum, oldArtist, newAlbum, newArtist, newYear)
        }
    }

    // --- PLAYLISTS ---

    fun createPlaylist(name: String) {
        viewModelScope.launch { playlistRepository.createPlaylist(Playlist(playlistName = name)) }
    }

    fun createPlaylistWithId(playlist: Playlist) {
        viewModelScope.launch { playlistRepository.createPlaylist(playlist) }
    }

    fun updatePlaylistName(playlistId: String, newName: String) {
        viewModelScope.launch { playlistRepository.updatePlaylistName(playlistId, newName) }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlistId) }
    }

    fun addSongToPlaylist(songId: String, playlistId: String, position: Int) {
        viewModelScope.launch { playlistRepository.addSongToPlaylist(songId, playlistId, position) }
    }

    fun addSongsToPlaylist(songIds: List<String>, playlistId: String, startPosition: Int) {
        viewModelScope.launch { playlistRepository.addSongsToPlaylist(songIds, playlistId, startPosition) }
    }

    fun removeSongFromPlaylist(songId: String, playlistId: String) {
        viewModelScope.launch { playlistRepository.removeSongFromPlaylist(songId, playlistId) }
    }

    fun reorderPlaylist(playlistId: String, orderedSongIds: List<String>) {
        viewModelScope.launch { playlistRepository.reorderPlaylist(playlistId, orderedSongIds) }
    }

    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>> =
        playlistRepository.getSongsInPlaylist(playlistId)

    fun getPlaylistSongCount(playlistId: String): Flow<Int> =
        playlistRepository.getPlaylistSongCount(playlistId)

    fun copyImageToInternalStorage(uri: android.net.Uri, playlistId: String): String? {
        return playlistRepository.copyImageToInternalStorage(uri, playlistId)
    }

    // --- VINYLS ---

    fun getFullVinylRecordById(vinylId: String): Flow<FullVinylRecord?> =
        vinylRepository.getFullVinylRecordById(vinylId)

    suspend fun createVinyl(vinyl: Vinyl) {
        vinylRepository.createVinyl(vinyl)
    }

    suspend fun createVinylSide(side: VinylSide) {
        vinylRepository.createVinylSide(side)
    }

    fun deleteVinyl(vinylId: String) {
        viewModelScope.launch(Dispatchers.IO) { vinylRepository.deleteVinyl(vinylId) }
    }

    fun updateVinylDetails(vinylId: String, title: String, artist: String?) {
        viewModelScope.launch(Dispatchers.IO) { vinylRepository.updateVinylDetails(vinylId, title, artist) }
    }

    fun updateVinylCoverArt(vinylId: String, coverArtUri: String?) {
        viewModelScope.launch(Dispatchers.IO) { vinylRepository.updateVinylCoverArt(vinylId, coverArtUri) }
    }

    fun updateVinylSideName(vinylSideId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) { vinylRepository.updateVinylSideName(vinylSideId, newName) }
    }

    fun deleteVinylSide(vinylSideId: String) {
        viewModelScope.launch(Dispatchers.IO) { vinylRepository.deleteVinylSide(vinylSideId) }
    }

    fun addSongToVinylSide(songId: String, vinylSideId: String, trackPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            vinylRepository.addSongToVinylSide(songId, vinylSideId, trackPosition)
        }
    }

    fun addSongsToVinylSide(songIds: List<String>, vinylSideId: String, startPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            vinylRepository.addSongsToVinylSide(songIds, vinylSideId, startPosition)
        }
    }

    fun removeSongFromVinylSide(vinylSideId: String, songId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            vinylRepository.removeSongFromVinylSide(vinylSideId, songId)
        }
    }

    fun reorderVinylSide(vinylSideId: String, orderedSongIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            vinylRepository.reorderVinylSide(vinylSideId, orderedSongIds)
        }
    }

    fun getSongsInVinylSide(vinylSideId: String): Flow<List<Song>> =
        vinylRepository.getSongsInVinylSide(vinylSideId)

    fun copyVinylImageToInternalStorage(uri: android.net.Uri, vinylId: String): String? {
        return vinylRepository.copyVinylImageToInternalStorage(uri, vinylId)
    }

    // --- LIBRARY MANAGEMENT ---

    var librarySize by mutableLongStateOf(0L)
        private set

    fun loadLibrarySize() {
        viewModelScope.launch(Dispatchers.IO) { librarySize = songRepository.getLibrarySizeBytes() }
    }

    fun clearLibrary() {
        viewModelScope.launch(Dispatchers.IO) {
            songRepository.clearLibrary()
            librarySize = 0L
        }
    }
}

class LibraryViewModelFactory(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val vinylRepository: VinylRepository,
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(songRepository, playlistRepository, vinylRepository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}