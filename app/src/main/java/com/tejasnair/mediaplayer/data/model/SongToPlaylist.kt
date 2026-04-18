package com.tejasnair.mediaplayer.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "songToPlaylist",
    primaryKeys = ["songId", "playlistId"],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["songId"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SongToPlaylist(
    val songId: String,
    val playlistId: String,
    val position: Int
)