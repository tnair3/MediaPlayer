package com.tejasnair.mediaplayer.data

import java.util.UUID

data class Artist(
    val id : String = UUID.randomUUID().toString(),
    val name : String
) {
    companion object {
        val UnknownArtist = Artist(name = "Unknown Artist")
    }
}