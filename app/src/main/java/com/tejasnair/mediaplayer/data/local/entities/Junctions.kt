package com.tejasnair.mediaplayer.data.local.entities

import androidx.room.Entity
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Junction
import com.tejasnair.mediaplayer.data.model.*

data class ArtistWithSongs(
    @Embedded val artist: Artist,
    @Relation(
        parentColumn = "id",         // The 'id' in the Artist table
        entityColumn = "id",         // The 'id' in the Song table
        associateBy = Junction(
            value = ArtistsForSong::class,
            parentColumn = "artistId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
)

data class SongWithArtists(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "id",         // Song ID
        entityColumn = "id",         // Artist ID
        associateBy = Junction(
            value = ArtistsForSong::class,
            parentColumn = "songId",
            entityColumn = "artistId"
        )
    )
    val artists: List<Artist>
)

data class AlbumWithSongs(
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