package com.tejasnair.mediaplayer.data.model

import com.tejasnair.mediaplayer.R
import androidx.annotation.DrawableRes
import java.util.UUID

data class Album(
    val id : String = UUID.randomUUID().toString(),
    val title : String,
    val albumArtist : Artist,
    val year : Int? = null,

    val albumArtUri: String? = null,
    @param:DrawableRes val albumArtRes: Int? = null
) {
    companion object {
        val UnknownAlbum = Album(
            id = "unknown",
            title = "Unknown Album",
            albumArtist = Artist.UnknownArtist,
            albumArtRes = R.drawable.unknown_album_art
        )
    }

    val artModel: Any?
        get() = albumArtUri
            ?: albumArtRes
            ?: UnknownAlbum.albumArtRes
}