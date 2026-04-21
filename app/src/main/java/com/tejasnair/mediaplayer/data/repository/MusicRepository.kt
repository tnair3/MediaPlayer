package com.tejasnair.mediaplayer.data.repository

import android.content.Context
import android.util.Log
import java.io.File
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.local.dao.MusicDao
import com.tejasnair.mediaplayer.data.model.*

class MusicRepository(
    private val musicDao: MusicDao,
    private val context: Context
) {

    // --- SONGS ---

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val favouriteSongs: Flow<List<Song>> = musicDao.getFavouriteSongs()

    fun getSongById(songId: String): Flow<Song?> = musicDao.getSongById(songId)
    suspend fun insert(song: Song) = musicDao.insertSong(song)
    suspend fun updateSong(song: Song) = musicDao.updateSong(song)
    suspend fun toggleFavourite(songId: String) = musicDao.toggleFavourite(songId)
    suspend fun findExistingSong(title: String, artist: String, album: String, albumArtist: String): Song? =
        musicDao.findExistingSong(title, artist, album, albumArtist)

    // --- ALBUMS ---

    val albums: Flow<List<AlbumSummary>> = musicDao.getUniqueAlbums()
    fun getSongsByAlbum(name: String, artist: String): Flow<List<Song>> = musicDao.getSongsByAlbum(name, artist)
    suspend fun updateAlbumDetails(oldAlbum: String, oldArtist: String, newAlbum: String, newArtist: String, newYear: String?) =
        musicDao.updateAlbumDetails(oldAlbum, oldArtist, newAlbum, newArtist, newYear)

    // --- PLAYLISTS ---

    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    suspend fun createPlaylist(playlist: Playlist) = musicDao.insertPlaylist(playlist)

    suspend fun updatePlaylistName(playlistId: String, newName: String) =
        musicDao.updatePlaylistName(playlistId, newName)

    suspend fun deletePlaylist(playlistId: String) = musicDao.deletePlaylist(playlistId)

    suspend fun addSongToPlaylist(songId: String, playlistId: String, position: Int) =
        musicDao.addSongToPlaylist(SongToPlaylist(songId, playlistId, position))

    suspend fun addSongsToPlaylist(songIds: List<String>, playlistId: String, startPosition: Int) {
        val crossRefs = songIds.mapIndexed { i, id ->
            SongToPlaylist(id, playlistId, startPosition + i)
        }
        musicDao.insertSongToPlaylistBatch(crossRefs)
    }

    suspend fun removeSongFromPlaylist(songId: String, playlistId: String) =
        musicDao.removeSongFromPlaylist(songId, playlistId)

    // Reorder: replace entire song list with a new ordered list
    suspend fun reorderPlaylist(playlistId: String, orderedSongIds: List<String>) {
        musicDao.clearPlaylistSongs(playlistId)
        val crossRefs = orderedSongIds.mapIndexed { i, id -> SongToPlaylist(id, playlistId, i) }
        musicDao.insertSongToPlaylistBatch(crossRefs)
    }

    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>> = musicDao.getSongsInPlaylist(playlistId)
    fun getPlaylistSongCount(playlistId: String): Flow<Int> = musicDao.getPlaylistSongCount(playlistId)

    // --- FILE CLEANUP ---

    suspend fun deleteSong(song: Song) {
        try { File(song.filePath).takeIf { it.exists() }?.delete() }
        catch (e: Exception) { Log.e("Repository", "Error deleting file: ${e.message}") }
        finally { musicDao.deleteSong(song) }
    }

    suspend fun clearLibrary() {
        musicDao.getAllSongsOnce().forEach { File(it.filePath).takeIf { f -> f.exists() }?.delete() }
        musicDao.clearLibrary()
    }

    suspend fun getLibrarySizeBytes(): Long =
        musicDao.getAllSongsOnce().sumOf { File(it.filePath).takeIf { f -> f.exists() }?.length() ?: 0L }
}