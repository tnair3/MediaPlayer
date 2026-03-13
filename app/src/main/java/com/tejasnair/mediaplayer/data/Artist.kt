package com.tejasnair.mediaplayer.data

data class Artist(
    val name: String
) {
    companion object {
        val UnknownArtist = Artist("Unknown Artist")
    }
}