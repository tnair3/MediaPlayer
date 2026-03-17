package com.tejasnair.mediaplayer.data.repository

import kotlinx.coroutines.flow.Flow
import com.tejasnair.mediaplayer.data.local.dao.MusicDao
import com.tejasnair.mediaplayer.data.model.*
import com.tejasnair.mediaplayer.data.local.entities.*

class MusicRepository(private val musicDao: MusicDao) {

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val allAlbums: Flow<List<Album>> = musicDao.getAllAlbums()
    val allArtists: Flow<List<Artist>> = musicDao.getAllArtists()

    fun getAlbumSongs(id: String) = musicDao.getAlbumDetail(id)
    fun getArtistSongs(id: String) = musicDao.getArtistDetail(id)

    suspend fun insertFullSongData(song: Song, artists: List<Artist>, album: Album?) {

        musicDao.insertSong(song)

        artists.forEach { artist ->
            musicDao.insertArtist(artist) // Saved if new
            musicDao.insertArtistsForSong(
                ArtistsForSong(songId = song.id, artistId = artist.id)
            )
        }

        album?.let {
            musicDao.insertAlbum(it)

            musicDao.insertSongsInAlbum(
                SongsInAlbum(albumId = it.id, songId = song.id)
            )

            musicDao.insertArtistsForAlbum(
                ArtistsForAlbum(albumId = it.id, artistId = it.albumArtistId)
            )
        }
    }
}