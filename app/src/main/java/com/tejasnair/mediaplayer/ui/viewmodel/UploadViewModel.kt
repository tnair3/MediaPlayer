package com.tejasnair.mediaplayer.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.tejasnair.mediaplayer.data.local.files.UploadWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class UploadProgress(
    val isUploading: Boolean = false,
    val completed: Int = 0,
    val total: Int = 0
) {
    val percent: Int get() = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0
}

class UploadViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    private val _uploadProgress = MutableStateFlow(UploadProgress())
    val uploadProgress: StateFlow<UploadProgress> = _uploadProgress.asStateFlow()

    val isUploading: StateFlow<Boolean> get() = MutableStateFlow(_uploadProgress.value.isUploading)

    private var sessionBatchTag: String? = null

    init {
        workManager.pruneWork()

        viewModelScope.launch {
            workManager.getWorkInfosByTagFlow("upload_worker").collect { infos ->
                val anyActive = infos.any {
                    it.state == WorkInfo.State.RUNNING ||
                            it.state == WorkInfo.State.ENQUEUED ||
                            it.state == WorkInfo.State.BLOCKED
                }
                if (anyActive && !_uploadProgress.value.isUploading) {
                    _uploadProgress.value = _uploadProgress.value.copy(isUploading = true)
                }
            }
        }
    }

    @SuppressLint("EnqueueWork")
    fun startUpload(uris: List<Uri>) {
        if (uris.isEmpty()) return

        val batchTag = "upload_batch_${UUID.randomUUID()}"
        sessionBatchTag = batchTag
        val total = uris.size

        val requests = uris.mapIndexed { index, uri ->
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(UploadWorker.KEY_URI, uri.toString())
                        .putInt(UploadWorker.KEY_INDEX, index)
                        .putInt(UploadWorker.KEY_TOTAL, total)
                        .build()
                )
                .addTag("upload_worker")
                .addTag(batchTag)
                .build()
        }

        _uploadProgress.value = UploadProgress(isUploading = true, completed = 0, total = total)

        val chain: WorkContinuation = requests.drop(1).fold(
            workManager.beginWith(requests.first())
        ) { continuation, request ->
            continuation.then(request)
        }
        chain.enqueue()

        viewModelScope.launch {
            workManager.getWorkInfosByTagFlow(batchTag).collect { infos ->
                if (infos.isEmpty()) return@collect

                val completed = infos.count { it.state == WorkInfo.State.SUCCEEDED }
                val anyActive = infos.any {
                    it.state == WorkInfo.State.RUNNING ||
                            it.state == WorkInfo.State.ENQUEUED ||
                            it.state == WorkInfo.State.BLOCKED
                }

                _uploadProgress.value = UploadProgress(
                    isUploading = anyActive,
                    completed = completed,
                    total = total
                )
            }
        }
    }

    fun cancelUpload() {
        sessionBatchTag?.let { workManager.cancelAllWorkByTag(it) }
        _uploadProgress.value = UploadProgress()
    }
}