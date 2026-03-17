package com.tejasnair.mediaplayer.data.model

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val duration: Int,
    val discNumber: Int = 1,
    val trackNumber: Int = 1,
    val isFavourite: Boolean = false,
    val songArtUri: String? = null,
    @param:DrawableRes val songArtRes: Int? = null,
    val albumId: String? = null
)