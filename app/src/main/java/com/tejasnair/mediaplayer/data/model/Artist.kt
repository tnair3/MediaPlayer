package com.tejasnair.mediaplayer.data.model

import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String
)