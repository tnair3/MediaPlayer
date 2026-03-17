package com.tejasnair.mediaplayer.data.local.entities

import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Junction
import com.tejasnair.mediaplayer.data.model.*

data class SongDetail(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ArtistsForSong::class,
            parentColumn = "songId",
            entityColumn = "artistId"
        )
    )
    val artists: List<Artist>
)

data class AlbumDetail(
    @Embedded val album: Album,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SongsInAlbum::class,
            parentColumn = "albumId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
)

data class ArtistDetail(
    @Embedded val artist: Artist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ArtistsForSong::class,
            parentColumn = "artistId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
)

data class ArtistDiscography(
    @Embedded val artist: Artist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ArtistsForAlbum::class,
            parentColumn = "artistId",
            entityColumn = "albumId"
        )
    )
    val album: List<Album>
)

@Entity(primaryKeys = ["albumId", "songId"])
data class SongsInAlbum(
    val albumId: String,
    val songId: String
)

@Entity(primaryKeys = ["songId", "artistId"])
data class ArtistsForSong(
    val songId: String,
    val artistId: String
)

@Entity(primaryKeys = ["albumId", "artistId"])
data class ArtistsForAlbum(
    val albumId: String,
    val artistId: String
)