package com.example.miyad.ui.product

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventFormLogicTest {
    private val zone = ZoneId.of("Asia/Kuala_Lumpur")

    @Test
    fun nativePickerValuesProduceExpectedSchedule() {
        val request = buildEventCreateRequest(
            values = validValues(),
            zoneId = zone,
        )

        assertEquals("2026-06-15T09:30+08:00", request.start_time)
        assertEquals("2026-06-15T11:00+08:00", request.end_time)
        assertEquals("weekly", request.repeat)
        assertNull(request.description)
        assertNull(request.location)
    }

    @Test(expected = IllegalArgumentException::class)
    fun optionalValidationRejectsEndBeforeStart() {
        buildEventCreateRequest(
            values = validValues().copy(endTime = LocalTime.of(8, 30)),
            zoneId = zone,
        )
    }

    @Test
    fun allDayPickerUsesNextMidnight() {
        val request = buildEventCreateRequest(
            values = validValues().copy(allDay = true),
            zoneId = zone,
        )

        assertEquals("2026-06-15T00:00+08:00", request.start_time)
        assertEquals("2026-06-16T00:00+08:00", request.end_time)
    }

    private fun validValues() = EventFormValues(
        title = "Database lab",
        description = " ",
        date = LocalDate.of(2026, 6, 15),
        startTime = LocalTime.of(9, 30),
        endTime = LocalTime.of(11, 0),
        allDay = false,
        repeat = "weekly",
        location = "",
        reminder = "one_day",
        color = "#B8F23A",
    )
}
