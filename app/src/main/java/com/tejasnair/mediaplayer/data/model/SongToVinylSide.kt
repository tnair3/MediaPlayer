package com.tejasnair.mediaplayer.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "songToVinylSide",
    primaryKeys = ["songId", "vinylSideId"],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["songId"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VinylSide::class,
            parentColumns = ["vinylSideId"],
            childColumns = ["vinylSideId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vinylSideId"])]
)
data class SongToVinylSide(
    val songId: String,
    val vinylSideId: String,
    val trackPosition: Int
)