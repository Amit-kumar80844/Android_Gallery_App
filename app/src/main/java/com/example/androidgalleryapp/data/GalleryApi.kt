package com.example.androidgalleryapp.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GalleryApi {
    @POST("api/demo/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @Multipart
    @POST("api/images/upload")
    suspend fun uploadImage(
        @Header("X-Device-Token") token: String,
        @Part("device_id") deviceId: RequestBody,
        @Part image: MultipartBody.Part
    ): UploadResponse

    @GET("api/health")
    suspend fun health(): Map<String, Any>
}

