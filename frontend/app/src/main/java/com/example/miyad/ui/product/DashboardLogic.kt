package com.example.miyad.ui.product

import com.example.miyad.data.EventDto
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class DashboardMetrics(
    val todayCount: Int,
    val weekCount: Int,
    val weekProgress: Float
)

fun dashboardMetrics(
    events: List<EventDto>,
    today: LocalDate = LocalDate.now()
): DashboardMetrics {
    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val endOfWeek = startOfWeek.plusDays(6)
    val eventDates = events.mapNotNull { it.localDateOrNull() }
    return DashboardMetrics(
        todayCount = eventDates.count { it == today },
        weekCount = eventDates.count { !it.isBefore(startOfWeek) && !it.isAfter(endOfWeek) },
        weekProgress = today.dayOfWeek.value / 7f
    )
}
