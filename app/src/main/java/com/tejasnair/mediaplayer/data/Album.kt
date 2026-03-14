package com.tejasnair.mediaplayer.data

import java.util.UUID

data class Album(
    val id : String = UUID.randomUUID().toString(),
    val title : String,
    val albumArtist : Artist,
    val year : Int? = null,
    var albumArtUri: String? = null,  // default from disc 1 track 1
) {
    companion object {
        val UnknownAlbum = Album(
            id = "unknown",
            title = "Unknown Album",
            albumArtist = Artist.UnknownArtist,
            year = 2026
        )
    }
}