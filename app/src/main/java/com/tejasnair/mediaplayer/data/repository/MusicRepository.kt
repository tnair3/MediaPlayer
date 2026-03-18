package com.tejasnair.mediaplayer.data.repository

import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.local.dao.MusicDao
import com.tejasnair.mediaplayer.data.model.*

class MusicRepository(private val musicDao: MusicDao) {

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val albums: Flow<List<AlbumSummary>> = musicDao.getUniqueAlbums()
    val artists: Flow<List<String>> = musicDao.getUniqueArtists()

    suspend fun insert(song: Song) {
        musicDao.insertSong(song)
    }

    suspend fun delete(song: Song) {
        musicDao.deleteSong(song)
    }

    fun getSongsByAlbum(name: String, artist: String) =
        musicDao.getSongsByAlbum(name, artist)
}