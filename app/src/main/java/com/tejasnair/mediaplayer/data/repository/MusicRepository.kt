package com.tejasnair.mediaplayer.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
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

    fun copyImageToInternalStorage(context: Context, uri: Uri, playlistId: String): String? {
        return try {
            val dir = File(context.filesDir, "playlist_art").apply { mkdirs() }
            val dest = File(dir, "cover_$playlistId.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (e: Exception) {
            Log.e("Repository", "Failed to copy playlist cover", e)
            null
        }
    }

    suspend fun removeSongFromPlaylist(songId: String, playlistId: String) =
        musicDao.removeSongFromPlaylist(songId, playlistId)

    suspend fun reorderPlaylist(playlistId: String, orderedSongIds: List<String>) {
        musicDao.clearPlaylistSongs(playlistId)
        val crossRefs = orderedSongIds.mapIndexed { i, id -> SongToPlaylist(id, playlistId, i) }
        musicDao.insertSongToPlaylistBatch(crossRefs)
    }

    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>> = musicDao.getSongsInPlaylist(playlistId)
    fun getPlaylistSongCount(playlistId: String): Flow<Int> = musicDao.getPlaylistSongCount(playlistId)

    // --- VINYLS ---

    val allVinyls: Flow<List<FullVinylRecord>> = musicDao.getAllFullVinylRecords()

    fun getFullVinylRecordById(vinylId: String): Flow<FullVinylRecord?> = musicDao.getFullVinylRecordById(vinylId)

    suspend fun createVinyl(vinyl: Vinyl) = musicDao.insertVinyl(vinyl)

    suspend fun createVinylSide(side: VinylSide) = musicDao.insertVinylSide(side)

    suspend fun deleteVinyl(vinylId: String) = musicDao.deleteVinylById(vinylId)

    suspend fun addSongToVinylSide(songId: String, vinylSideId: String, trackPosition: Int) =
        musicDao.insertSongToVinylSide(SongToVinylSide(songId, vinylSideId, trackPosition))

    suspend fun addSongsToVinylSide(songIds: List<String>, vinylSideId: String, startPosition: Int) {
        val crossRefs = songIds.mapIndexed { i, id ->
            SongToVinylSide(id, vinylSideId, startPosition + i)
        }
        musicDao.insertSongToVinylSideBatch(crossRefs)
    }

    suspend fun removeSongFromVinylSide(vinylSideId: String, songId: String) =
        musicDao.removeSongFromVinylSide(vinylSideId, songId)

    suspend fun reorderVinylSide(vinylSideId: String, orderedSongIds: List<String>) {
        musicDao.clearVinylSideSongs(vinylSideId)
        val crossRefs = orderedSongIds.mapIndexed { i, id -> SongToVinylSide(id, vinylSideId, i) }
        musicDao.insertSongToVinylSideBatch(crossRefs)
    }

    fun getSongsInVinylSide(vinylSideId: String): Flow<List<Song>> = musicDao.getSongsInVinylSide(vinylSideId)

    fun copyVinylImageToInternalStorage(context: Context, uri: Uri, vinylId: String): String? {
        return try {
            val dir = File(context.filesDir, "vinyl_art").apply { mkdirs() }
            val dest = File(dir, "vinyl_$vinylId.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (e: Exception) {
            Log.e("Repository", "Failed to copy vinyl cover", e)
            null
        }
    }

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