package com.example.miyad.data

import com.example.miyad.BuildConfig
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.OffsetDateTime
import java.time.ZoneId

class MiyadRepository(private val tokenStore: TokenStore) {
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            tokenStore.token?.let { header("Authorization", "Bearer $it") }
        }.build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
        )
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()
        .create(MiyadApi::class.java)

    suspend fun login(email: String, password: String): UserDto {
        val response = api.login(LoginRequest(email.trim(), password))
        tokenStore.token = response.token
        tokenStore.user = response.user
        return response.user
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        university: String
    ): UserDto {
        val response = api.register(
            RegisterRequest(email.trim(), password, name.trim(), university.trim())
        )
        tokenStore.token = response.token
        tokenStore.user = response.user
        return response.user
    }

    suspend fun events(type: String? = null): List<EventDto> =
        api.events(type = type, language = tokenStore.language).events

    suspend fun deleteEvent(id: String) {
        api.deleteEvent(id)
    }

    suspend fun updateReminder(id: String, reminder: String): EventDto =
        api.updateEvent(id, EventUpdateRequest(reminder = reminder))

    suspend fun extract(text: String, save: Boolean): ExtractionResponse =
        api.extractManual(
            ManualExtractRequest(
                raw_content = text,
                timestamp = OffsetDateTime.now(ZoneId.systemDefault()).toString(),
                timezone = ZoneId.systemDefault().id,
                save = save
            )
        )

    suspend fun settings(): UserSettingsDto = api.settings()

    suspend fun updateSettings(update: UserSettingsUpdate): UserSettingsDto =
        api.updateSettings(update)

    fun logout() = tokenStore.clearSession()

    fun friendlyError(error: Throwable): String {
        if (error is HttpException) {
            return when (error.code()) {
                401 -> "Invalid email or password"
                409 -> "This email is already registered"
                502, 503 -> "The extraction service is temporarily unavailable"
                else -> "Request failed (${error.code()})"
            }
        }
        return error.message ?: "Network connection failed"
    }
}
