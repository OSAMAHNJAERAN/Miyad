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

    suspend fun createEvent(request: EventCreateRequest): EventDto =
        api.createEvent(request)

    suspend fun deleteEvent(id: String) {
        api.deleteEvent(id)
    }

    suspend fun updateReminder(id: String, reminder: String): EventDto =
        api.updateEvent(id, EventUpdateRequest(reminder = reminder))

    suspend fun updateEvent(id: String, request: EventCreateRequest): EventDto =
        api.updateEvent(
            id,
            EventUpdateRequest(
                title = request.title,
                event_type = request.event_type,
                description = request.description ?: "",
                due_date = request.start_time,
                start_time = request.start_time,
                end_time = request.end_time,
                all_day = request.all_day,
                repeat = request.repeat,
                notes = request.description ?: "",
                location = request.location ?: "",
                reminder = request.reminder,
                event_color = request.event_color
            )
        )

    suspend fun extract(text: String, save: Boolean): ExtractionResponse =
        api.extractManual(
            ManualExtractRequest(
                raw_content = text,
                timestamp = OffsetDateTime.now(ZoneId.systemDefault()).toString(),
                timezone = ZoneId.systemDefault().id,
                save = save
            )
        )

    suspend fun confirmReviewedExtraction(
        text: String,
        events: List<EventDto>
    ): ExtractionResponse =
        api.confirmManualExtraction(
            ConfirmManualExtractionRequest(
                raw_content = text,
                timestamp = OffsetDateTime.now(ZoneId.systemDefault()).toString(),
                timezone = ZoneId.systemDefault().id,
                events = events
            )
        )

    suspend fun settings(): UserSettingsDto = api.settings()

    suspend fun updateSettings(update: UserSettingsUpdate): UserSettingsDto =
        api.updateSettings(update)

    suspend fun getCourses(): List<CourseDto> = api.getCourses()

    suspend fun createCourse(request: CourseCreateDto): CourseDto = api.createCourse(request)

    suspend fun deleteCourse(courseCode: String) {
        api.deleteCourse(courseCode)
    }

    suspend fun getSchedule(): List<ScheduleDto> = api.getSchedule()

    suspend fun createSchedule(request: ScheduleCreateDto): ScheduleDto = api.createSchedule(request)

    suspend fun deleteSchedule(slotId: String) {
        api.deleteSchedule(slotId)
    }

    suspend fun getAlerts(statusFilter: String? = "pending"): List<VerificationAlertDto> =
        api.getAlerts(statusFilter)

    suspend fun resolveAlert(alertId: String, action: String): VerificationAlertDto =
        api.resolveAlert(alertId, AlertResolutionDto(action))

    fun logout() = tokenStore.clearSession()

    fun friendlyError(error: Throwable): String {
        if (error is HttpException) {
            return when (error.code()) {
                401 -> "Invalid email or password"
                409 -> "This email is already registered"
                422 -> "Check the event date, time, and required fields"
                502, 503 -> "The extraction service is temporarily unavailable"
                else -> "Request failed (${error.code()})"
            }
        }
        return error.message ?: "Network connection failed"
    }
}
