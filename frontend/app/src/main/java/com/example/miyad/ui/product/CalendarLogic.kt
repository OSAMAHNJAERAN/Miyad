package com.example.miyad.ui.product

import com.example.miyad.data.EventDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

data class CalendarDay(
    val date: LocalDate,
    val isInDisplayedMonth: Boolean
)

fun monthCalendarDays(
    month: YearMonth,
    firstDayOfWeek: DayOfWeek = DayOfWeek.SATURDAY
): List<CalendarDay> {
    val firstOfMonth = month.atDay(1)
    var gridStart = firstOfMonth.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    if (gridStart.isAfter(firstOfMonth)) {
        gridStart = gridStart.minusWeeks(1)
    }
    return List(42) { offset ->
        val date = gridStart.plusDays(offset.toLong())
        CalendarDay(date, YearMonth.from(date) == month)
    }
}

fun EventDto.localDateOrNull(): LocalDate? =
    runCatching { OffsetDateTime.parse(due_date).toLocalDate() }.getOrNull()

fun eventsForDate(
    events: List<EventDto>,
    date: LocalDate,
    type: String? = null
): List<EventDto> = events
    .filter { it.localDateOrNull() == date && (type == null || it.event_type == type) }
    .sortedBy { it.due_date }
