package com.tejasnair.mediaplayer.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.local.dao.VinylDao
import com.tejasnair.mediaplayer.data.model.FullVinylRecord
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.model.SongToVinylSide
import com.tejasnair.mediaplayer.data.model.Vinyl
import com.tejasnair.mediaplayer.data.model.VinylSide

class VinylRepository(
    private val vinylDao: VinylDao,
    private val context: Context
) {

    val allVinyls: Flow<List<FullVinylRecord>> = vinylDao.getAllFullVinylRecords()

    fun getFullVinylRecordById(vinylId: String): Flow<FullVinylRecord?> = vinylDao.getFullVinylRecordById(vinylId)

    suspend fun createVinyl(vinyl: Vinyl) = vinylDao.insertVinyl(vinyl)

    suspend fun createVinylSide(side: VinylSide) = vinylDao.insertVinylSide(side)

    suspend fun deleteVinyl(vinylId: String) = vinylDao.deleteVinylById(vinylId)

    suspend fun updateVinylDetails(vinylId: String, title: String, artist: String?) =
        vinylDao.updateVinylDetails(vinylId, title, artist)

    suspend fun updateVinylCoverArt(vinylId: String, coverArtUri: String?) =
        vinylDao.updateVinylCoverArt(vinylId, coverArtUri)

    suspend fun updateVinylSideName(vinylSideId: String, newName: String) =
        vinylDao.updateVinylSideName(vinylSideId, newName)

    suspend fun deleteVinylSide(vinylSideId: String) =
        vinylDao.deleteVinylSideById(vinylSideId)

    suspend fun addSongToVinylSide(songId: String, vinylSideId: String, trackPosition: Int) =
        vinylDao.insertSongToVinylSide(SongToVinylSide(songId, vinylSideId, trackPosition))

    suspend fun addSongsToVinylSide(songIds: List<String>, vinylSideId: String, startPosition: Int) {
        val crossRefs = songIds.mapIndexed { i, id ->
            SongToVinylSide(id, vinylSideId, startPosition + i)
        }
        vinylDao.insertSongToVinylSideBatch(crossRefs)
    }

    suspend fun removeSongFromVinylSide(vinylSideId: String, songId: String) =
        vinylDao.removeSongFromVinylSide(vinylSideId, songId)

    suspend fun reorderVinylSide(vinylSideId: String, orderedSongIds: List<String>) {
        vinylDao.clearVinylSideSongs(vinylSideId)
        val crossRefs = orderedSongIds.mapIndexed { i, id -> SongToVinylSide(id, vinylSideId, i) }
        vinylDao.insertSongToVinylSideBatch(crossRefs)
    }

    fun getSongsInVinylSide(vinylSideId: String): Flow<List<Song>> = vinylDao.getSongsInVinylSide(vinylSideId)

    fun copyVinylImageToInternalStorage(uri: Uri, vinylId: String): String? {
        return try {
            val dir = File(context.filesDir, "vinyl_art").apply { mkdirs() }
            val dest = File(dir, "vinyl_$vinylId.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (e: Exception) {
            Log.e("VinylRepository", "Failed to copy vinyl cover", e)
            null
        }
    }
}