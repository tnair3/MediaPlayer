package com.tejasnair.mediaplayer.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.local.dao.PlaylistDao
import com.tejasnair.mediaplayer.data.model.Playlist
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.model.SongToPlaylist

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val context: Context
) {

    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(playlist: Playlist) = playlistDao.insertPlaylist(playlist)

    suspend fun updatePlaylistName(playlistId: String, newName: String) =
        playlistDao.updatePlaylistName(playlistId, newName)

    suspend fun deletePlaylist(playlistId: String) = playlistDao.deletePlaylist(playlistId)

    suspend fun addSongToPlaylist(songId: String, playlistId: String, position: Int) =
        playlistDao.addSongToPlaylist(SongToPlaylist(songId, playlistId, position))

    suspend fun addSongsToPlaylist(songIds: List<String>, playlistId: String, startPosition: Int) {
        val crossRefs = songIds.mapIndexed { i, id ->
            SongToPlaylist(id, playlistId, startPosition + i)
        }
        playlistDao.insertSongToPlaylistBatch(crossRefs)
    }

    suspend fun removeSongFromPlaylist(songId: String, playlistId: String) =
        playlistDao.removeSongFromPlaylist(songId, playlistId)

    suspend fun reorderPlaylist(playlistId: String, orderedSongIds: List<String>) {
        playlistDao.clearPlaylistSongs(playlistId)
        val crossRefs = orderedSongIds.mapIndexed { i, id -> SongToPlaylist(id, playlistId, i) }
        playlistDao.insertSongToPlaylistBatch(crossRefs)
    }

    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>> = playlistDao.getSongsInPlaylist(playlistId)
    fun getPlaylistSongCount(playlistId: String): Flow<Int> = playlistDao.getPlaylistSongCount(playlistId)

    fun copyImageToInternalStorage(uri: Uri, playlistId: String): String? {
        return try {
            val dir = File(context.filesDir, "playlist_art").apply { mkdirs() }
            val dest = File(dir, "cover_$playlistId.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (e: Exception) {
            Log.e("PlaylistRepository", "Failed to copy playlist cover", e)
            null
        }
    }
}