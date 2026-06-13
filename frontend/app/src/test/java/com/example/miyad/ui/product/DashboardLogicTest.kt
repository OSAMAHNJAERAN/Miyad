package com.example.miyad.ui.product

import com.example.miyad.data.EventDto
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DashboardLogicTest {
    @Test
    fun countsTodayAndCurrentWeek() {
        val events = listOf(
            event("2026-06-11T10:00:00+08:00"),
            event("2026-06-12T10:00:00+08:00"),
            event("2026-06-15T10:00:00+08:00")
        )

        val metrics = dashboardMetrics(events, LocalDate.of(2026, 6, 11))

        assertEquals(1, metrics.todayCount)
        assertEquals(2, metrics.weekCount)
        assertEquals(4f / 7f, metrics.weekProgress)
    }

    private fun event(dueDate: String) = EventDto(
        title = "Event",
        course_code = null,
        event_type = "other",
        due_date = dueDate,
        location = null,
        notes = null
    )
}
