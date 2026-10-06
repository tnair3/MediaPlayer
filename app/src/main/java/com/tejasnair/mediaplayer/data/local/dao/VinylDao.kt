package com.tejasnair.mediaplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.model.FullVinylRecord
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.model.SongToVinylSide
import com.tejasnair.mediaplayer.data.model.Vinyl
import com.tejasnair.mediaplayer.data.model.VinylSide

@Dao
interface VinylDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVinyl(vinyl: Vinyl): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVinylSide(side: VinylSide): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToVinylSide(crossRef: SongToVinylSide)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToVinylSideBatch(crossRefs: List<SongToVinylSide>)

    @Transaction
    @Query("SELECT * FROM vinyls WHERE vinylId = :vinylId")
    fun getFullVinylRecordById(vinylId: String): Flow<FullVinylRecord?>

    @Transaction
    @Query("SELECT * FROM vinyls ORDER BY dateCreated DESC")
    fun getAllFullVinylRecords(): Flow<List<FullVinylRecord>>

    @Query("DELETE FROM vinyls WHERE vinylId = :vinylId")
    suspend fun deleteVinylById(vinylId: String)

    @Query("UPDATE vinyls SET title = :title, artist = :artist WHERE vinylId = :vinylId")
    suspend fun updateVinylDetails(vinylId: String, title: String, artist: String?)

    @Query("UPDATE vinyls SET coverArtUri = :coverArtUri WHERE vinylId = :vinylId")
    suspend fun updateVinylCoverArt(vinylId: String, coverArtUri: String?)

    @Query("UPDATE vinylSides SET sideName = :newName WHERE vinylSideId = :vinylSideId")
    suspend fun updateVinylSideName(vinylSideId: String, newName: String)

    @Query("DELETE FROM vinylSides WHERE vinylSideId = :vinylSideId")
    suspend fun deleteVinylSideById(vinylSideId: String)

    @Query("DELETE FROM songToVinylSide WHERE vinylSideId = :vinylSideId")
    suspend fun clearVinylSideSongs(vinylSideId: String)

    @Query("DELETE FROM songToVinylSide WHERE vinylSideId = :vinylSideId AND songId = :songId")
    suspend fun removeSongFromVinylSide(vinylSideId: String, songId: String)

    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN songToVinylSide ON songs.songId = songToVinylSide.songId
        WHERE songToVinylSide.vinylSideId = :vinylSideId
        ORDER BY songToVinylSide.trackPosition ASC
    """)
    fun getSongsInVinylSide(vinylSideId: String): Flow<List<Song>>
}