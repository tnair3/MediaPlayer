package com.tejasnair.mediaplayer.data.model
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "vinylSides",
    foreignKeys = [
        ForeignKey(
            entity = Vinyl::class,
            parentColumns = ["vinylId"],
            childColumns = ["vinylId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vinylId"])]
)
data class VinylSide(
    @PrimaryKey val vinylSideId: String = UUID.randomUUID().toString(),
    val vinylId: String,
    val position: Int,
    val sideName: String,
)