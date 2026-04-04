package com.tejasnair.mediaplayer.data.local.files

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.scale
import com.shabinder.jaudiotagger.audio.AudioFileIO
import com.shabinder.jaudiotagger.tag.FieldKey
import com.shabinder.jaudiotagger.tag.images.Artwork
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

    // Public Entry Point

    suspend fun scanAudioFile(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val tempFile = copyToInternalStorage(fileUri, "temp_${System.currentTimeMillis()}")
                    ?: run {
                        showToast("Failed to import file — could not read input")
                        return@withContext
                    }

                val song = try {
                    extractMetadata(tempFile)
                } catch (e: Exception) {
                    Log.w("MediaScanner", "Metadata extraction failed", e)
                    tempFile.delete()
                    showToast("Failed to import file — could not read metadata")
                    return@withContext
                }

                val extension = tempFile.extension
                val formattedName = "${song.album} - ${song.title} (${song.artists}) ${song.discNumber}.${song.trackNumber}.$extension"
                    .replace(Regex("[\\\\/:*?\"<>|]"), "_")

                val finalFile = File(tempFile.parentFile, formattedName)

                val destinationFile = if (tempFile.renameTo(finalFile)) finalFile else tempFile

                val updatedSong = song.copy(filePath = destinationFile.absolutePath)

                val isDuplicate = repository.findExistingSong(
                    updatedSong.title,
                    updatedSong.artists,
                    updatedSong.album,
                    updatedSong.albumArtists
                ) != null

                if (isDuplicate) {
                    destinationFile.delete()
                    showToast("${updatedSong.title} is already in your library")
                    return@withContext
                }

                repository.insert(updatedSong)
                Log.d("MediaScanner", "Imported: ${updatedSong.title} — ${destinationFile.absolutePath}")

            } catch (e: Exception) {
                Log.e("MediaScanner", "Unexpected import error", e)
                showToast("Failed to import file")
            }
        }
    }

    // File Copying

    private fun copyToInternalStorage(fileUri: Uri, fileName: String): File? {
        val extension = resolveExtension(fileUri)
        val musicFolder = File(context.filesDir, "music_library").apply { mkdirs() }
        val destination = File(musicFolder, "$fileName.$extension")

        return try {
            context.contentResolver.openInputStream(fileUri)?.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                    output.flush()
                    output.fd.sync()
                }
            } ?: return null
            destination
        } catch (e: Exception) {
            Log.e("MediaScanner", "Failed to copy file", e)
            destination.delete()
            null
        }
    }

    private fun resolveExtension(fileUri: Uri): String {
        val mime = context.contentResolver.getType(fileUri) ?: return "mp3"
        return when {
            mime.contains("flac") -> "flac"
            mime.contains("wav")  -> "wav"
            mime.contains("m4a") || mime.contains("mp4") -> "m4a"
            else -> "mp3"
        }
    }

    // Metadata Extraction

    private fun extractMetadata(file: File): Song {
        return try {
            extractWithJAudioTagger(file)
        } catch (e: Exception) {
            Log.w("MediaScanner", "JAudioTagger failed for ${file.name}, falling back", e)
            extractWithRetriever(file)
        }
    }

    private fun extractWithJAudioTagger(file: File): Song {
        val audioFile = AudioFileIO.read(file)
        val tag = audioFile.tag
        val header = audioFile.audioHeader

        fun field(key: FieldKey) = tag?.getFirst(key)?.takeIf { it.isNotBlank() }

        val title       = field(FieldKey.TITLE)       ?: file.nameWithoutExtension
        val artist      = field(FieldKey.ARTIST)      ?: "Unknown Artist"
        val album       = field(FieldKey.ALBUM)       ?: "Unknown Album"
        val albumArtist = field(FieldKey.ALBUM_ARTIST) ?: artist
        val year        = field(FieldKey.YEAR)
        val track       = field(FieldKey.TRACK)?.parseTrackComponent()  ?: 1
        val disc        = field(FieldKey.DISC_NO)?.parseTrackComponent() ?: 1
        val duration    = (header.trackLength * 1000).toLong()

        val savedArtPath = extractArt(tag?.firstArtwork, file.nameWithoutExtension)

        Log.d("MediaScanner", "JAudioTagger — title=$title artist=$artist album=$album")

        return Song(
            filePath     = file.absolutePath,
            title        = title,
            artists      = artist,
            album        = album,
            albumArtists = albumArtist,
            duration     = duration,
            discNumber   = disc,
            trackNumber  = track,
            year         = year,
            songArtUri   = savedArtPath
        )
    }

    private fun extractWithRetriever(file: File): Song {
        val retriever = android.media.MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)

            fun meta(key: Int) = retriever.extractMetadata(key)

            val artist = meta(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Unknown Artist"
            val album  = meta(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Unknown Album"

            val artPath = try {
                retriever.embeddedPicture?.let {
                    saveArtToInternalStorage(downscaleArtIfNeeded(it, 600), file.nameWithoutExtension)
                }
            } catch (e: Exception) { null }

            return Song(
                filePath     = file.absolutePath,
                title        = meta(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: file.nameWithoutExtension,
                artists      = artist,
                album        = album,
                albumArtists = meta(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: artist,
                duration     = meta(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
                discNumber   = meta(android.media.MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)?.parseTrackComponent() ?: 1,
                trackNumber  = meta(android.media.MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.parseTrackComponent() ?: 1,
                year         = meta(android.media.MediaMetadataRetriever.METADATA_KEY_YEAR),
                songArtUri   = artPath
            )
        } finally {
            retriever.release()
        }
    }

    // Art Handling

    private fun extractArt(artwork: Artwork?, uniqueKey: String): String? {
        artwork ?: return null
        return try {
            val scaled = downscaleArtIfNeeded(artwork.binaryData, maxDimension = 600)
            saveArtToInternalStorage(scaled, uniqueKey)
        } catch (e: Exception) {
            Log.w("MediaScanner", "Art extraction failed: ${e.message}")
            null
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
            Log.w("MediaScanner", "Art downscale failed", e)
            bytes
        }
    }

    private fun saveArtToInternalStorage(bytes: ByteArray, uniqueKey: String): String {
        val dir = File(context.filesDir, "album_art").apply { mkdirs() }
        val file = File(dir, "art_${uniqueKey.hashCode()}.jpg")
        FileOutputStream(file).use { it.write(bytes) }
        return file.absolutePath
    }

    // Helpers

    private fun String.parseTrackComponent(): Int? =
        trim().substringBefore('/').trimStart('0').toIntOrNull()

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}