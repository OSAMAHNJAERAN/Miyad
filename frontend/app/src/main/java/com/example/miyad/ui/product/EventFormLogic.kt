package com.example.miyad.ui.product

import com.example.miyad.data.EventCreateRequest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class EventFormValues(
    val title: String,
    val description: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val allDay: Boolean,
    val repeat: String,
    val location: String,
    val reminder: String,
    val color: String,
)

fun buildEventCreateRequest(
    values: EventFormValues,
    zoneId: ZoneId = ZoneId.systemDefault(),
): EventCreateRequest {
    require(values.title.isNotBlank()) { "title" }
    val start = if (values.allDay) {
        values.date.atStartOfDay()
    } else {
        LocalDateTime.of(values.date, values.startTime)
    }
    val end = if (values.allDay) {
        values.date.plusDays(1).atStartOfDay()
    } else {
        LocalDateTime.of(values.date, values.endTime)
    }
    require(end.isAfter(start)) { "time" }

    return EventCreateRequest(
        title = values.title.trim(),
        description = values.description.trim().ifBlank { null },
        start_time = start.atZone(zoneId).toOffsetDateTime().toString(),
        end_time = end.atZone(zoneId).toOffsetDateTime().toString(),
        all_day = values.allDay,
        repeat = values.repeat,
        location = values.location.trim().ifBlank { null },
        reminder = values.reminder,
        event_color = values.color,
    )
}
