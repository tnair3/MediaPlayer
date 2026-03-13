package com.tejasnair.mediaplayer.data

import java.util.UUID

data class Song(
    val id : String = UUID.randomUUID().toString(),
    val title : String,
    val artist : String,
    val duration : Int, // Seconds
    val discNumber : Int = 1,
    val trackNumber : Int = 1,
    val album : Album? = null,
    val songArtUri: String? = null   // always the song's own art
)