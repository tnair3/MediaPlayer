package com.tejasnair.mediaplayer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.model.Playlist
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.model.SongToPlaylist

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("UPDATE playlists SET playlistName = :newName WHERE playlistId = :playlistId")
    suspend fun updatePlaylistName(playlistId: String, newName: String)

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("SELECT * FROM playlists ORDER BY playlistName ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: SongToPlaylist)

    @Query("DELETE FROM songToPlaylist WHERE songId = :songId AND playlistId = :playlistId")
    suspend fun removeSongFromPlaylist(songId: String, playlistId: String)

    @Query("DELETE FROM songToPlaylist WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylistBatch(crossRefs: List<SongToPlaylist>)

    @Query("SELECT COUNT(*) FROM songToPlaylist WHERE playlistId = :playlistId")
    fun getPlaylistSongCount(playlistId: String): Flow<Int>

    @Query("""
        SELECT songs.* FROM songs 
        INNER JOIN songToPlaylist ON songs.songId = songToPlaylist.songId 
        WHERE songToPlaylist.playlistId = :playlistId 
        ORDER BY songToPlaylist.position ASC
    """)
    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>>
}