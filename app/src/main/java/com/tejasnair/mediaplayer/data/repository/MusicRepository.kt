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

    // --- CORE SONG DATA ---

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val favouriteSongs: Flow<List<Song>> = musicDao.getFavouriteSongs()

    fun getSongById(songId: String): Flow<Song?> = musicDao.getSongById(songId)

    suspend fun insert(song: Song) = musicDao.insertSong(song)

    suspend fun updateSong(song: Song) = musicDao.updateSong(song)

    suspend fun toggleFavourite(songId: String) = musicDao.toggleFavourite(songId)

    suspend fun findExistingSong(title: String, artist: String, album: String, albumArtist: String): Song? =
        musicDao.findExistingSong(title, artist, album, albumArtist)


    // --- ALBUMS & ARTISTS ---

    val albums: Flow<List<AlbumSummary>> = musicDao.getUniqueAlbums()

    fun getSongsByAlbum(name: String, artist: String): Flow<List<Song>> =
        musicDao.getSongsByAlbum(name, artist)

    fun getSongsByArtist(artistName: String): Flow<List<Song>> =
        musicDao.getSongsByArtist(artistName)

    suspend fun updateAlbumDetails(oldAlbum: String, oldArtist: String, newAlbum: String, newArtist: String, newYear: String?) =
        musicDao.updateAlbumDetails(oldAlbum, oldArtist, newAlbum, newArtist, newYear)


    // --- PLAYLIST MANAGEMENT ---

    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    suspend fun createPlaylist(playlist: Playlist) = musicDao.insertPlaylist(playlist)

    suspend fun addSongToPlaylist(songId: String, playlistId: String, position: Int) {
        val crossRef = SongToPlaylist(songId, playlistId, position)
        musicDao.addSongToPlaylist(crossRef)
    }

    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>> =
        musicDao.getSongsInPlaylist(playlistId)


    // --- STORAGE & FILE CLEANUP ---

    suspend fun deleteSong(song: Song) {
        try {
            val audioFile = File(song.filePath)
            if (audioFile.exists()) {
                audioFile.delete()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error deleting physical file: ${e.message}")
        } finally {
            musicDao.deleteSong(song)
        }
    }

    suspend fun clearLibrary() {
        val allSongs = musicDao.getAllSongsOnce()
        allSongs.forEach { song ->
            val file = File(song.filePath)
            if (file.exists()) file.delete()
        }
        musicDao.clearLibrary()
    }

    suspend fun getLibrarySizeBytes(): Long {
        return musicDao.getAllSongsOnce().sumOf { song ->
            val file = File(song.filePath)
            if (file.exists()) file.length() else 0L
        }
    }
}