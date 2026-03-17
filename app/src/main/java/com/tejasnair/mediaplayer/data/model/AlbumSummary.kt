package com.tejasnair.mediaplayer.data.model

data class AlbumSummary(
    val album: String,
    val albumArtists: String,
    val songArtUri: String?,
    val backCoverUri: String?
)