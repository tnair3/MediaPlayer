package com.tejasnair.mediaplayer.data.model

import androidx.annotation.DrawableRes
import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val year: Int? = null,
    val albumArtistId: String,
    val albumArtUri: String? = null,
    @param:DrawableRes val albumArtRes: Int? = null
)