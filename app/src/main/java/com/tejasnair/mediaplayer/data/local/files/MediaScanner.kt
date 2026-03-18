package com.tejasnair.mediaplayer.data.local.files

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaScanner(
    private val context: Context,
    private val repository: MusicRepository
) {
    suspend fun scanAudioFile(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, fileUri)

                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown Title"
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
                val albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: artist
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
                val discNum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)?.toIntOrNull() ?: 1
                val trackNum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toInt() ?: 1
                val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)

                val artBytes = retriever.embeddedPicture
                val savedArtPath = if (artBytes != null) {
                    saveArtToInternalStorage(artBytes, album, artist)
                } else null

                val song = Song(
                    filePath = fileUri.toString(),
                    title = title,
                    duration = duration,
                    artists = artist,
                    album = album,
                    albumArtists = albumArtist,
                    discNumber = discNum,
                    trackNumber = trackNum,
                    year = year,
                    songArtUri = savedArtPath,
                    backCoverUri = null // In a later build use a library like TagLib to specifically identify the 'Back Cover' byte array.
                )

                repository.insert(song)

            } catch (e: Exception) {
                Log.e("MediaScanner", "Failed to scan file: $fileUri", e)
            } finally {
                retriever.release()
            }
        }
    }

    private fun saveArtToInternalStorage(bytes: ByteArray, album: String, artist: String): String {
        val fileName = "art_${album.hashCode()}_${artist.hashCode()}.jpg"
        val directory = File(context.filesDir, "album_art")
        if (!directory.exists()) directory.mkdirs()

        val file = File(directory, fileName)
        if (!file.exists()) {
            FileOutputStream(file).use { fos ->
                fos.write(bytes)
            }
        }
        return file.absolutePath
    }
}