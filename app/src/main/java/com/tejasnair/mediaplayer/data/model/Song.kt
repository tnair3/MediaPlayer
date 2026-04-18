package com.tejasnair.mediaplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val songId: String = UUID.randomUUID().toString(),
    val filePath: String,
    val title: String,
    val duration: Long, // milliseconds
    val artists: String,
    val album: String,
    val albumArtists: String,
    val discNumber: Int = 1,
    val trackNumber: Int = 1,
    var isFavourite: Boolean = false,
    val year: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),

    val songArtUri: String? = null,
    val backCoverUri: String? = null
)