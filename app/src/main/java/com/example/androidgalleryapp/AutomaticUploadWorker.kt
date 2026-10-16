package com.example.androidgalleryapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.androidgalleryapp.data.GalleryRepository
import com.example.androidgalleryapp.data.SecureTokenStore

class AutomaticUploadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    private val channelId = "automatic_demo_uploads"

    override suspend fun doWork(): Result {
        val uris = inputData.getStringArray(KEY_URIS)?.map(Uri::parse).orEmpty().take(4)
        if (uris.isEmpty()) return Result.failure()
        createChannel()
        setForeground(foregroundInfo("Starting automatic demo upload…", 0))
        val prefs = applicationContext.getSharedPreferences("app_preferences", 0)
        val repository = GalleryRepository(applicationContext.contentResolver, SecureTokenStore(applicationContext)) {
            prefs.getString("base_url", "http://192.168.1.25:8000/") ?: "http://192.168.1.25:8000/"
        }
        return try {
            repository.registerIfNeeded()
            uris.forEachIndexed { index, uri ->
                repository.upload(uri)
                val completed = index + 1
                setProgress(androidx.work.workDataOf("completed" to completed))
                setForeground(foregroundInfo("Automatic demo upload: $completed/${uris.size}", completed * 100 / uris.size))
            }
            Result.success()
        } catch (_: Exception) {
            setForeground(foregroundInfo("Automatic demo upload failed", 0))
            Result.failure()
        }
    }

    private fun createChannel() {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(channelId, "Automatic demo uploads", NotificationManager.IMPORTANCE_LOW))
    }

    private fun foregroundInfo(text: String, progress: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Automatic demo mode")
            .setContentText(text)
            .setProgress(100, progress.coerceIn(0, 100), false)
            .setOngoing(progress < 100)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    companion object {
        const val WORK_NAME = "automatic_demo_upload"
        const val KEY_URIS = "selected_uris"
        private const val NOTIFICATION_ID = 4101
    }
}

