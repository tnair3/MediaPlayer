package com.tejasnair.mediaplayer.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class VinylSideWithSongs(
    @Embedded val side: VinylSide,
    @Relation(
        parentColumn = "vinylSideId",
        entityColumn = "songId",
        associateBy = Junction(value = SongToVinylSide::class)
    )
    val songs: List<Song>
)

data class FullVinylRecord(
    @Embedded val vinyl: Vinyl,
    @Relation(
        entity = VinylSide::class,
        parentColumn = "vinylId",
        entityColumn = "vinylId"
    )
    val sides: List<VinylSideWithSongs>
)