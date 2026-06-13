package com.example.miyad.ui.product

import com.example.miyad.data.EventDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarLogicTest {
    @Test
    fun monthGridAlwaysContainsSixCompleteWeeks() {
        val days = monthCalendarDays(YearMonth.of(2026, 6))

        assertEquals(42, days.size)
        assertEquals(DayOfWeek.SATURDAY, days.first().date.dayOfWeek)
        assertEquals(DayOfWeek.FRIDAY, days.last().date.dayOfWeek)
        assertTrue(days.any { it.date == LocalDate.of(2026, 6, 1) })
        assertFalse(days.first().isInDisplayedMonth)
    }

    @Test
    fun eventsAreGroupedByLocalDueDateAndType() {
        val events = listOf(
            event("exam", "2026-06-16T09:00:00+08:00"),
            event("deadline", "2026-06-16T23:59:00+08:00"),
            event("exam", "2026-06-17T09:00:00+08:00")
        )

        assertEquals(2, eventsForDate(events, LocalDate.of(2026, 6, 16)).size)
        assertEquals(
            1,
            eventsForDate(events, LocalDate.of(2026, 6, 16), "exam").size
        )
    }

    private fun event(type: String, date: String) = EventDto(
        id = "$type-$date",
        title = type,
        course_code = null,
        event_type = type,
        due_date = date,
        location = null,
        notes = null
    )
}
