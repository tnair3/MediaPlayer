package com.tejasnair.mediaplayer.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vinyls")
data class Vinyl(
    @PrimaryKey val vinylId: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String?,
    val coverArtUri: String?,
    val dateCreated: Long = System.currentTimeMillis()
)