package com.tejasnair.mediaplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val playlistId: String = UUID.randomUUID().toString(),
    val playlistName: String,
    val artUri: String? = null,
    val mosaicSongIds: String? = null // comma-separated
)