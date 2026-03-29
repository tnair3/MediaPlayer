package com.tejasnair.mediaplayer.data.local.files

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.scale
import com.mpatric.mp3agic.Mp3File
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
    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    suspend fun scanAudioFile(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val musicFolder = File(context.filesDir, "music_library")
                if (!musicFolder.exists()) musicFolder.mkdirs()

                val originalName = fileUri.lastPathSegment ?: ""
                val extension = originalName.substringAfterLast('.', "mp3")
                val fileName = "track_${System.currentTimeMillis()}.$extension"
                val destinationFile = File(musicFolder, fileName)

                context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                        outputStream.flush()
                        outputStream.fd.sync()
                    }
                } ?: run {
                    showToast("Failed to read file — could not open input stream")
                    return@withContext
                }

                Log.d("MediaScanner", "File exists: ${destinationFile.exists()}")
                Log.d("MediaScanner", "File size: ${destinationFile.length()} bytes")
                Log.d("MediaScanner", "File readable: ${destinationFile.canRead()}")

                val song = try {
                    extractMetadata(destinationFile)
                } catch (e: Exception) {
                    Log.e("MediaScanner", "Metadata extraction failed for ${destinationFile.name}", e)
                    destinationFile.delete()
                    showToast("Failed to import $originalName — could not read metadata")
                    return@withContext
                }

                val isDuplicate = repository.findExistingSong(
                    song.title,
                    song.artists,
                    song.album,
                    song.albumArtists
                ) != null
                if (isDuplicate) {
                    Log.d("MediaScanner", "Duplicate detected, skipping: ${song.title} by ${song.artists}")
                    destinationFile.delete()
                    showToast("${song.title} is already in your library")
                    return@withContext
                }

                repository.insert(song)
                Log.d("MediaScanner", "Saved to internal storage: ${destinationFile.absolutePath}")

            } catch (e: Exception) {
                Log.e("MediaScanner", "Failed to import file: $fileUri", e)
                showToast("Failed to import file — an unexpected error occurred")
            }
        }
    }

    private fun extractMetadata(file: File): Song {
        return try {
            extractWithMp3agic(file)
        } catch (e: Exception) {
            Log.w("MediaScanner", "mp3agic failed for ${file.name}, falling back to MediaMetadataRetriever", e)
            extractWithRetriever(file)
        }
    }

    private fun extractWithMp3agic(file: File): Song {
        val mp3 = Mp3File(file)

        val title: String
        val artist: String
        val album: String
        val albumArtist: String
        val year: String?
        val track: Int
        val disc: Int
        val artBytes: ByteArray?

        when {
            mp3.hasId3v2Tag() -> {
                val tag = mp3.id3v2Tag
                title       = tag.title?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
                artist      = tag.artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
                album       = tag.album?.takeIf { it.isNotBlank() } ?: "Unknown Album"
                albumArtist = tag.albumArtist?.takeIf { it.isNotBlank() } ?: artist
                year        = tag.year?.takeIf { it.isNotBlank() }
                track       = tag.track?.parseTrackComponent() ?: 1
                disc        = tag.partOfSet?.parseTrackComponent() ?: 1
                artBytes    = tag.albumImage
            }
            mp3.hasId3v1Tag() -> {
                val tag = mp3.id3v1Tag
                title       = tag.title?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
                artist      = tag.artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
                album       = tag.album?.takeIf { it.isNotBlank() } ?: "Unknown Album"
                albumArtist = artist
                year        = tag.year?.takeIf { it.isNotBlank() }
                track       = tag.track?.parseTrackComponent() ?: 1
                disc        = 1
                artBytes    = null
            }
            else -> {
                title       = file.nameWithoutExtension
                artist      = "Unknown Artist"
                album       = "Unknown Album"
                albumArtist = "Unknown Artist"
                year        = null
                track       = 1
                disc        = 1
                artBytes    = null
            }
        }

        Log.d("MediaScanner", "--- Metadata dump for: ${file.name} ---")
        Log.d("MediaScanner", "Title:  $title")
        Log.d("MediaScanner", "Artist: $artist")
        Log.d("MediaScanner", "Album:  $album")
        Log.d("MediaScanner", "Disc:  $disc")
        Log.d("MediaScanner", "Track:  $track")

        val savedArtPath = if (artBytes != null) {
            try {
                val scaled = downscaleArtIfNeeded(artBytes, maxDimension = 600)
                saveArtToInternalStorage(scaled, file.nameWithoutExtension)
            } catch (e: Exception) {
                Log.w("MediaScanner", "Art extraction failed for ${file.name}: ${e.message}")
                null
            }
        } else null

        return Song(
            filePath     = file.absolutePath,
            title        = title,
            artists      = artist,
            album        = album,
            albumArtists = albumArtist,
            duration     = mp3.lengthInMilliseconds,
            discNumber   = disc,
            trackNumber  = track,
            year         = year,
            songArtUri   = savedArtPath
        )
    }

    private fun extractWithRetriever(file: File): Song {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Unknown Album"

            val savedArtPath = try {
                val artBytes = retriever.embeddedPicture
                if (artBytes != null) {
                    val scaled = downscaleArtIfNeeded(artBytes, maxDimension = 600)
                    saveArtToInternalStorage(scaled, file.nameWithoutExtension)
                } else null
            } catch (e: Exception) {
                Log.w("MediaScanner", "Fallback art extraction failed for ${file.name}: ${e.message}")
                null
            }

            return Song(
                filePath     = file.absolutePath,
                title        = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: file.nameWithoutExtension,
                artists      = artist,
                album        = album,
                albumArtists = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                    ?: artist,
                duration     = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L,
                discNumber   = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                    ?.parseTrackComponent() ?: 1,
                trackNumber  = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                    ?.parseTrackComponent() ?: 1,
                year         = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR),
                songArtUri   = savedArtPath
            )
        } finally {
            retriever.release()
        }
    }

    private fun downscaleArtIfNeeded(bytes: ByteArray, maxDimension: Int): ByteArray {
        return try {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return bytes
            val scale = minOf(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height,
                1.0f
            )
            if (scale >= 1.0f) return bytes

            val scaled = bitmap.scale(
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt()
            )
            val out = java.io.ByteArrayOutputStream()
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
            bitmap.recycle()
            scaled.recycle()
            out.toByteArray()
        } catch (e: Exception) {
            Log.w("MediaScanner", "Art downscale failed, using original", e)
            bytes
        }
    }

    private fun String.parseTrackComponent(): Int? =
        this.trim()
            .substringBefore('/')
            .trimStart('0')
            .toIntOrNull()

    private fun saveArtToInternalStorage(bytes: ByteArray, uniqueKey: String): String {
        val directory = File(context.filesDir, "album_art")
        if (!directory.exists()) directory.mkdirs()

        val fileName = "art_${uniqueKey.hashCode()}.jpg"
        val file = File(directory, fileName)

        FileOutputStream(file).use { fos ->
            fos.write(bytes)
        }

        return file.absolutePath
    }
}