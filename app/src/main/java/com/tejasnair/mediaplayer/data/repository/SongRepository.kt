package com.tejasnair.mediaplayer.data.repository

import android.content.Context
import android.util.Log
import java.io.File
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.local.dao.SongDao
import com.tejasnair.mediaplayer.data.model.AlbumSummary
import com.tejasnair.mediaplayer.data.model.Song

class SongRepository(
    private val songDao: SongDao,
    private val context: Context
) {

    // --- SONGS ---

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favouriteSongs: Flow<List<Song>> = songDao.getFavouriteSongs()

    fun getSongById(songId: String): Flow<Song?> = songDao.getSongById(songId)
    suspend fun insert(song: Song) = songDao.insertSong(song)
    suspend fun updateSong(song: Song) = songDao.updateSong(song)
    suspend fun toggleFavourite(songId: String) = songDao.toggleFavourite(songId)
    suspend fun findExistingSong(title: String, artist: String, album: String, albumArtist: String): Song? =
        songDao.findExistingSong(title, artist, album, albumArtist)

    // --- ALBUMS ---

    val albums: Flow<List<AlbumSummary>> = songDao.getUniqueAlbums()
    fun getSongsByAlbum(name: String, artist: String): Flow<List<Song>> = songDao.getSongsByAlbum(name, artist)
    suspend fun updateAlbumDetails(oldAlbum: String, oldArtist: String, newAlbum: String, newArtist: String, newYear: String?) =
        songDao.updateAlbumDetails(oldAlbum, oldArtist, newAlbum, newArtist, newYear)

    // --- FILE CLEANUP & STORAGE ---

    suspend fun deleteSong(song: Song) {
        try {
            File(song.filePath).takeIf { it.exists() }?.delete()
        } catch (e: Exception) {
            Log.e("SongRepository", "Error deleting file: ${e.message}")
        } finally {
            songDao.deleteSong(song)
        }
    }

    suspend fun clearLibrary() {
        songDao.getAllSongsOnce().forEach { File(it.filePath).takeIf { f -> f.exists() }?.delete() }
        songDao.clearLibrary()
    }

    suspend fun getLibrarySizeBytes(): Long =
        songDao.getAllSongsOnce().sumOf { File(it.filePath).takeIf { f -> f.exists() }?.length() ?: 0L }
}