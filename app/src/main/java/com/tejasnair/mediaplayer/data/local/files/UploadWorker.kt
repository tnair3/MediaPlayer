package com.tejasnair.mediaplayer.data.local.files

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.tejasnair.mediaplayer.R
import com.tejasnair.mediaplayer.data.local.database.MusicDatabase
import com.tejasnair.mediaplayer.data.repository.MusicRepository

class UploadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_URI = "upload_uri"
        const val KEY_INDEX = "upload_index"
        const val KEY_TOTAL = "upload_total"
        const val CHANNEL_ID = "upload_channel"
        const val NOTIFICATION_ID_PROGRESS = 201
        const val NOTIFICATION_ID_DONE = 202
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun doWork(): Result {
        val index = inputData.getInt(KEY_INDEX, 0)
        val total = inputData.getInt(KEY_TOTAL, 1)
        val isLast = index == total - 1

        return try {
            val uriString = inputData.getString(KEY_URI)
                ?: return Result.success()

            val uri = try {
                uriString.toUri()
            } catch (e: Exception) {
                Log.e("UploadWorker", "Invalid URI: $uriString", e)
                if (isLast) { cancelProgressNotification(); showDoneNotification(total) }
                return Result.success()
            }

            setForeground(buildForegroundInfo(index, total))

            val database = MusicDatabase.getDatabase(context)
            val repository = MusicRepository(database.musicDao(), context)
            val scanner = MediaScanner(context, repository)

            scanner.scanAudioFile(uri)

            releasePermission(uri)
            System.gc()

            if (isLast) {
                cancelProgressNotification()
                showDoneNotification(total)
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("UploadWorker", "Worker failed at index $index", e)
            if (isLast) {
                cancelProgressNotification()
                showDoneNotification(total)
            }
            Result.success()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildForegroundInfo(completed: Int, total: Int): ForegroundInfo {
        createChannel()
        val percent = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Importing songs")
            .setContentText("$percent%  ·  $completed of $total")
            .setSmallIcon(R.drawable.nav_upload)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total, completed, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        return ForegroundInfo(
            NOTIFICATION_ID_PROGRESS,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun cancelProgressNotification() {
        notificationManager.cancel(NOTIFICATION_ID_PROGRESS)
    }

    private fun showDoneNotification(total: Int) {
        createChannel()
        val text = if (total == 1) "1 song imported" else "$total songs imported"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Import complete")
            .setContentText(text)
            .setSmallIcon(R.drawable.nav_upload)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(NOTIFICATION_ID_DONE, notification)
    }

    private fun releasePermission(uri: Uri) {
        try {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {}
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Song imports",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Shows progress while importing songs" }
        notificationManager.createNotificationChannel(channel)
    }
}