package com.tejasnair.mediaplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.model.AlbumSummary
import com.tejasnair.mediaplayer.data.model.Song

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM songs WHERE songId = :id")
    suspend fun deleteSongById(id: String)

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsOnce(): List<Song>

    @Query("SELECT * FROM songs WHERE songId = :id LIMIT 1")
    fun getSongById(id: String): Flow<Song?>

    @Query("SELECT * FROM songs WHERE isFavourite = 1 ORDER BY title ASC")
    fun getFavouriteSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE title = :title AND artists = :artist AND album = :album AND albumArtists = :albumArtist LIMIT 1")
    suspend fun findExistingSong(title: String, artist: String, album: String, albumArtist: String): Song?

    @Query("SELECT * FROM songs WHERE album = :albumName AND albumArtists = :albumArtist ORDER BY trackNumber ASC")
    fun getSongsByAlbum(albumName: String, albumArtist: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE artists LIKE '%' || :artistName || '%'")
    fun getSongsByArtist(artistName: String): Flow<List<Song>>

    @Query("SELECT DISTINCT album, albumArtists, songArtUri, backCoverUri, year FROM songs GROUP BY album, albumArtists ORDER BY album ASC")
    fun getUniqueAlbums(): Flow<List<AlbumSummary>>

    @Query("SELECT DISTINCT artists FROM songs ORDER BY artists ASC")
    fun getUniqueArtists(): Flow<List<String>>

    @Query("UPDATE songs SET album = :newAlbum, albumArtists = :newArtist, year = :newYear WHERE album = :oldAlbum AND albumArtists = :oldArtist")
    suspend fun updateAlbumDetails(oldAlbum: String, oldArtist: String, newAlbum: String, newArtist: String, newYear: String?)

    @Query("UPDATE songs SET isFavourite = NOT isFavourite WHERE songId = :songId")
    suspend fun toggleFavourite(songId: String)

    @Query("DELETE FROM songs")
    suspend fun clearLibrary()
}