package com.tejasnair.mediaplayer.ui.components

import com.tejasnair.mediaplayer.R

sealed class SortOption(val label: String, val iconRes: Int) {
    object SongName : SortOption(label = "Name", iconRes = R.drawable.sort_name)
    object SongAlbum : SortOption(label = "Album", iconRes = R.drawable.sort_album)
    object SongArtist : SortOption(label = "Artist", iconRes = R.drawable.sort_artist)
    object SongYear : SortOption(label = "Year", iconRes = R.drawable.sort_year)

    object AlbumName : SortOption(label = "Name", iconRes = R.drawable.sort_name)
    object AlbumArtist : SortOption(label = "Artist", iconRes = R.drawable.sort_artist)
    object AlbumYear : SortOption(label = "Year", iconRes = R.drawable.sort_year)

    object ArtistName : SortOption(label = "Name", iconRes = R.drawable.sort_name)
    object ArtistSongCount : SortOption(label = "Number of Songs", iconRes = R.drawable.sort_number)
}