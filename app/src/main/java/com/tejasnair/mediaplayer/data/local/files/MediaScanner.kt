package com.tejasnair.mediaplayer.data.local.files

// 1. Android & Core
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.scale

// 2. Java & IO
import java.io.File
import java.io.FileOutputStream

// 3. Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 4. External Libraries (JAudioTagger)
import com.shabinder.jaudiotagger.audio.AudioFileIO
import com.shabinder.jaudiotagger.tag.FieldKey
import com.shabinder.jaudiotagger.tag.images.Artwork

// 5. Local Project Imports
import com.tejasnair.mediaplayer.data.model.Song
import com.tejasnair.mediaplayer.data.repository.MusicRepository

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
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            val header = audioFile.audioHeader

            fun getField(key: FieldKey): String? = tag?.getFirst(key)?.takeIf { it.isNotBlank() }

            val title = getField(FieldKey.TITLE) ?: file.nameWithoutExtension
            val artist = getField(FieldKey.ARTIST) ?: "Unknown Artist"
            val album = getField(FieldKey.ALBUM) ?: "Unknown Album"

            // Artwork extraction
            val artwork: Artwork? = tag?.firstArtwork
            val savedArtPath = if (artwork != null) {
                try {
                    val artBytes = artwork.binaryData
                    val scaled = downscaleArtIfNeeded(artBytes, maxDimension = 600)
                    saveArtToInternalStorage(scaled, file.nameWithoutExtension)
                } catch (e: Exception) {
                    Log.w("MediaScanner", "Art extraction failed: ${e.message}")
                    null
                }
            } else null

            Song(
                filePath     = file.absolutePath,
                title        = title,
                artists      = artist,
                album        = album,
                albumArtists = getField(FieldKey.ALBUM_ARTIST) ?: artist,
                duration     = (header.trackLength * 1000).toLong(), // Convert seconds to ms
                discNumber   = getField(FieldKey.DISC_NO)?.toIntOrNull() ?: 1,
                trackNumber  = getField(FieldKey.TRACK)?.toIntOrNull() ?: 1,
                year         = getField(FieldKey.YEAR),
                songArtUri   = savedArtPath
            )
        } catch (e: Exception) {
            Log.e("MediaScanner", "JAudioTagger failed for ${file.name}, falling back", e)
            extractWithRetriever(file)
        }
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