package com.example.androidgalleryapp.data

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class GalleryRepository(
    private val resolver: ContentResolver,
    private val tokenStore: SecureTokenStore,
    private val getBaseUrl: () -> String
) {
    private fun api(): GalleryApi = Retrofit.Builder()
        .baseUrl(getBaseUrl().trimEnd('/') + "/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GalleryApi::class.java)

    suspend fun registerIfNeeded(): String {
        val existingToken = tokenStore.token
        val existingId = tokenStore.deviceId
        if (existingToken != null && existingId != null) return existingId
        val id = existingId ?: "phone-${UUID.randomUUID()}"
        val response = api().register(RegisterRequest(id, "User Android Phone"))
        tokenStore.deviceId = response.device_id
        tokenStore.token = response.device_token
        return response.device_id
    }

    suspend fun upload(uri: Uri): UploadResponse {
        val deviceId = tokenStore.deviceId ?: registerIfNeeded()
        val token = tokenStore.token ?: error("Device is not registered")
        val mime = resolver.getType(uri)?.lowercase() ?: "image/jpeg"
        require(mime in setOf("image/jpeg", "image/png", "image/webp")) { "Only JPEG, PNG, and WebP images are supported." }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Unable to read selected image")
        val filename = "image-${System.currentTimeMillis()}.${mime.substringAfter('/', "jpg")}" 
        val body = bytes.toRequestBody(mime.toMediaType())
        return api().uploadImage(token, deviceId.toRequestBody("text/plain".toMediaType()), MultipartBody.Part.createFormData("image", filename, body))
    }

    suspend fun checkHealth() = api().health()
}

