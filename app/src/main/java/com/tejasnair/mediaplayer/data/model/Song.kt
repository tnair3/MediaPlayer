package com.tejasnair.mediaplayer.data.model

import androidx.annotation.DrawableRes
import com.tejasnair.mediaplayer.R
import java.util.UUID

data class Song(
    val id : String = UUID.randomUUID().toString(),
    val title : String,
    val artist : Artist,
    val duration : Int, // Seconds
    val discNumber : Int = 1,
    val trackNumber : Int = 1,
    val album : Album? = Album.UnknownAlbum,
    val isFavourite : Boolean = false,

    val songArtUri : String? = null,
    @param:DrawableRes val songArtRes: Int? = null
) {
    val artModel: Any
        get() = songArtUri
            ?: songArtRes
            ?: R.drawable.unknown_song_art
}