package com.tejasnair.mediaplayer.ui.components

import com.tejasnair.mediaplayer.R

sealed class SortOption(val label: String, val iconRes: Int) {
    object SongName : SortOption("Name", R.drawable.sort_name)
    object SongAlbum : SortOption("Album", R.drawable.sort_album)
    object SongArtist : SortOption("Artist", R.drawable.sort_artist)
    object SongYear : SortOption("Year", R.drawable.sort_year)

    object AlbumName : SortOption("Name", R.drawable.sort_name)
    object AlbumArtist : SortOption("Artist", R.drawable.sort_artist)
    object AlbumYear : SortOption("Year", R.drawable.sort_year)

    object ArtistName : SortOption("Name", R.drawable.sort_name)
    object ArtistSongCount : SortOption("Number of Songs", R.drawable.sort_number)

    object PlaylistName : SortOption("Name", R.drawable.sort_name)
    object PlaylistSongCount : SortOption("Number of Songs", R.drawable.sort_number)
}