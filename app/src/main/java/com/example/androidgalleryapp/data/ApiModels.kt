package com.example.androidgalleryapp.data

data class RegisterRequest(val device_id: String, val name: String)
data class RegisterResponse(val status: String, val device_id: String, val device_token: String)
data class UploadResponse(val status: String, val image_id: Long, val filename: String)

