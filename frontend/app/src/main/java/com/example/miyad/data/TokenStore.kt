package com.example.miyad.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

class TokenStore(context: Context) {
    private val gson = Gson()
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "miyad_secure_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var token: String?
        get() = preferences.getString("token", null)
        set(value) {
            preferences.edit().apply {
                if (value == null) remove("token") else putString("token", value)
            }.apply()
        }

    var user: UserDto?
        get() = preferences.getString("user", null)?.let {
            runCatching { gson.fromJson(it, UserDto::class.java) }.getOrNull()
        }
        set(value) {
            preferences.edit().apply {
                if (value == null) remove("user") else putString("user", gson.toJson(value))
            }.apply()
        }

    var language: String
        get() = preferences.getString("language", "ar") ?: "ar"
        set(value) {
            preferences.edit().putString("language", value).apply()
        }

    var onboardingComplete: Boolean
        get() = preferences.getBoolean("onboarding_complete", false)
        set(value) {
            preferences.edit().putBoolean("onboarding_complete", value).apply()
        }

    fun clearSession() {
        preferences.edit().remove("token").remove("user").apply()
    }
}
