package com.tejasnair.mediaplayer.data.repository

// 1. Android & Core
import android.content.Context
import android.util.Log

// 2. Java IO
import java.io.File

// 3. Coroutines & Flow
import kotlinx.coroutines.flow.Flow

// 4. Local Project Imports
import com.tejasnair.mediaplayer.data.local.dao.MusicDao
import com.tejasnair.mediaplayer.data.model.*

class MusicRepository(
    private val musicDao: MusicDao,
    private val context: Context
) {

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val albums: Flow<List<AlbumSummary>> = musicDao.getUniqueAlbums()
    val artists: Flow<List<String>> = musicDao.getUniqueArtists()

    val favouriteSongs: Flow<List<Song>> = musicDao.getFavouriteSongs()

    fun getSongById(songId: String): Flow<Song?> {
        return musicDao.getSongById(songId)
    }

    suspend fun insert(song: Song) {
        musicDao.insertSong(song)
    }

    suspend fun toggleFavourite(songId: String) {
        musicDao.toggleFavourite(songId)
    }

    suspend fun deleteSong(song: Song) {
        try {
            val audioFile = File(song.filePath)
            if (audioFile.exists()) {
                audioFile.delete()
            }

            musicDao.deleteSong(song)
        }
        catch (e: Exception) {
            Log.e("Repository", "Error deleting files: ${e.message}")
            musicDao.deleteSong(song)
        }
    }

    suspend fun clearLibrary() {
        val allSongs = musicDao.getAllSongsOnce()

        allSongs.forEach { song ->
            val file = File(song.filePath)
            if (file.exists()) {
                file.delete()
            }
        }

        musicDao.clearLibrary()
    }

    suspend fun getLibrarySizeBytes(): Long {
        return musicDao.getAllSongsOnce().sumOf { song ->
            val file = File(song.filePath)
            if (file.exists()) file.length() else 0L
        }
    }

    suspend fun findExistingSong(title: String, artist: String, album: String, albumArtist: String): Song? =
        musicDao.findExistingSong(title, artist, album, albumArtist)

    fun getSongsByAlbum(name: String, artist: String) =
        musicDao.getSongsByAlbum(name, artist)
}