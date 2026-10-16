package com.example.androidgalleryapp.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureTokenStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_device_preferences",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var token: String?
        get() = prefs.getString("device_token", null)
        set(value) { prefs.edit().putString("device_token", value).apply() }

    var deviceId: String?
        get() = prefs.getString("device_id", null)
        set(value) { prefs.edit().putString("device_id", value).apply() }
}

