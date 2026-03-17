package com.tejasnair.mediaplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.local.entities.*
import com.tejasnair.mediaplayer.data.model.*

@Dao
interface MusicDao {

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<Artist>>

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    fun getSongDetail(artistId: String): Flow<SongDetail>

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    fun getAlbumDetail(artistId: String): Flow<AlbumDetail>

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    fun getArtistDetail(artistId: String): Flow<ArtistDetail>

    @Transaction
    @Query("SELECT * FROM artists WHERE id = :artistId")
    fun getArtistDiscography(artistId: String): Flow<ArtistDiscography>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtist(artist: Artist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtistsForSong(crossRef: ArtistsForSong)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtistsForAlbum(crossRef: ArtistsForAlbum)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongsInAlbum(crossRef: SongsInAlbum)
}