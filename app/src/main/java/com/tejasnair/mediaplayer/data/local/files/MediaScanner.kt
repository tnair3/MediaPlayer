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
import kotlin.text.toLong

class MediaScanner(
    private val context: Context,
    private val repository: MusicRepository
) {
    suspend fun scanAudioFile(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {

                val musicFolder = File(context.filesDir, "music_library")
                if (!musicFolder.exists()) musicFolder.mkdirs()

                val fileName = "track_${System.currentTimeMillis()}.mp3"
                val destinationFile = File(musicFolder, fileName)

                context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val song = extractMetadata(destinationFile)

                repository.insert(song)

                Log.d("MediaScanner", "Successfully saved to internal storage: ${destinationFile.absolutePath}")

            } catch (e: Exception) {
                Log.e("MediaScanner", "Failed to copy and scan file: $fileUri", e)
            }
        }
    }

    private fun extractMetadata(file: File): Song {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)

        val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"

        // Use your existing helper to save art correctly
        val artBytes = retriever.embeddedPicture
        val savedArtPath = if (artBytes != null) {
            saveArtToInternalStorage(artBytes, album, artist)
        } else null

        val song = Song(
            filePath = file.absolutePath, // THE INTERNAL PATH
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.nameWithoutExtension,
            artists = artist,
            album = album,
            albumArtists = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: artist,
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0,
            discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)?.toIntOrNull() ?: 1,
            trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toInt() ?: 1,
            year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR),
            songArtUri = savedArtPath
        )
        retriever.release()
        return song
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