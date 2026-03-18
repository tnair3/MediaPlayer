package com.tejasnair.mediaplayer.data.repository

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.local.dao.MusicDao
import com.tejasnair.mediaplayer.data.model.*
import java.io.File
import android.util.Log

class MusicRepository(
    private val musicDao: MusicDao,
    private val context: Context
) {

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val albums: Flow<List<AlbumSummary>> = musicDao.getUniqueAlbums()
    val artists: Flow<List<String>> = musicDao.getUniqueArtists()

    suspend fun insert(song: Song) {
        musicDao.insertSong(song)
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

    fun getSongsByAlbum(name: String, artist: String) =
        musicDao.getSongsByAlbum(name, artist)
}