package com.example.miyad.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MiyadApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("api/events")
    suspend fun events(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null,
        @Query("type") type: String? = null,
        @Query("language") language: String? = null
    ): EventsResponse

    @POST("api/events")
    suspend fun createEvent(@Body request: EventCreateRequest): EventDto

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") id: String): DeleteResponse

    @PATCH("api/events/{id}")
    suspend fun updateEvent(
        @Path("id") id: String,
        @Body request: EventUpdateRequest
    ): EventDto

    @POST("api/extract-manual")
    suspend fun extractManual(@Body request: ManualExtractRequest): ExtractionResponse

    @GET("api/settings")
    suspend fun settings(): UserSettingsDto

    @PATCH("api/settings")
    suspend fun updateSettings(@Body request: UserSettingsUpdate): UserSettingsDto
}
