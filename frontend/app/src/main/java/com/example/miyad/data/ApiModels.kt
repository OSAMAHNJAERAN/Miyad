package com.example.miyad.data

data class UserDto(
    val id: String,
    val email: String,
    val name: String?,
    val university: String?,
    val preferred_language: String = "ar"
)

data class AuthResponse(val user: UserDto, val token: String)
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val university: String
)

data class EventDto(
    val id: String = "",
    val title: String,
    val course_code: String?,
    val course_name: String? = null,
    val assignment_name: String? = null,
    val event_type: String,
    val due_date: String,
    val location: String?,
    val lecturer: String? = null,
    val notes: String?,
    val confidence: String = "medium",
    val source_email_subject: String? = null,
    val source_email_sender: String? = null,
    val evidence: String? = null,
    val source_hash: String? = null,
    val created_at: String = "",
    val reminder: String? = null,
    val description: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val all_day: Boolean = false,
    val repeat: String = "none",
    val event_color: String = "#BCDA4B"
)

data class EventsResponse(val events: List<EventDto>)
data class DeleteResponse(val success: Boolean)
data class EventCreateRequest(
    val title: String,
    val description: String?,
    val start_time: String,
    val end_time: String,
    val all_day: Boolean,
    val repeat: String,
    val location: String?,
    val reminder: String,
    val event_color: String,
    val event_type: String = "other"
)

data class EventUpdateRequest(
    val title: String? = null,
    val event_type: String? = null,
    val description: String? = null,
    val due_date: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val all_day: Boolean? = null,
    val repeat: String? = null,
    val notes: String? = null,
    val location: String? = null,
    val reminder: String? = null,
    val event_color: String? = null
)

data class ManualExtractRequest(
    val raw_content: String,
    val subject: String = "Manual extraction",
    val sender: String = "mobile-app",
    val timestamp: String,
    val timezone: String,
    val save: Boolean
)

data class ExtractionResponse(
    val status: String,
    val events_created: Int,
    val events: List<EventDto>
)

data class ConfirmManualExtractionRequest(
    val raw_content: String,
    val subject: String = "Manual extraction",
    val sender: String = "mobile-app",
    val timestamp: String,
    val timezone: String,
    val events: List<EventDto>
)

data class UserSettingsDto(
    val preferred_language: String = "ar",
    val notifications_enabled: Boolean = true,
    val reminder_same_day: Boolean = true,
    val reminder_one_day: Boolean = true,
    val reminder_one_week: Boolean = false,
    val extension_connected: Boolean = false
)

data class UserSettingsUpdate(
    val preferred_language: String? = null,
    val notifications_enabled: Boolean? = null,
    val reminder_same_day: Boolean? = null,
    val reminder_one_day: Boolean? = null,
    val reminder_one_week: Boolean? = null
)

data class CourseDto(
    val id: String,
    val user_id: String,
    val course_code: String,
    val course_name: String,
    val teaching_plan: String?,
    val created_at: String
)

data class CourseCreateDto(
    val course_code: String,
    val course_name: String,
    val teaching_plan: String?
)

data class ScheduleDto(
    val id: String,
    val user_id: String,
    val course_code: String,
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val location: String?,
    val created_at: String
)

data class ScheduleCreateDto(
    val course_code: String,
    val day_of_week: String,
    val start_time: String,
    val end_time: String,
    val location: String?
)

data class VerificationAlertDto(
    val id: String,
    val user_id: String,
    val event_data: EventDto,
    val email_hash: String,
    val alert_type: String,
    val ai_reason: String?,
    val confidence: String,
    val status: String,
    val created_at: String,
    val resolved_at: String?
)

data class AlertResolutionDto(
    val action: String
)
