package com.tejasnair.mediaplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.model.*

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("""SELECT DISTINCT album, albumArtists, songArtUri, backCoverUri, year FROM songs GROUP BY album, albumArtists ORDER BY album ASC""")
    fun getUniqueAlbums(): Flow<List<AlbumSummary>>

    @Query("SELECT DISTINCT artists FROM songs ORDER BY artists ASC")
    fun getUniqueArtists(): Flow<List<String>>

    @Query("SELECT * FROM songs WHERE album = :albumName AND albumArtists = :albumArtist ORDER BY trackNumber ASC")
    fun getSongsByAlbum(albumName: String, albumArtist: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE artists LIKE '%' || :artistName || '%'")
    fun getSongsByArtist(artistName: String): Flow<List<Song>>

    @Query("DELETE FROM songs WHERE songId = :id")
    suspend fun deleteSong(id: String)

    @Delete
    suspend fun deleteSong(song: Song)
}