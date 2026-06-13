package com.example.miyad.notifications

import com.example.miyad.data.EventDto
import com.example.miyad.data.UserSettingsDto
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

enum class ReminderKind(val storageValue: String) {
    SameDay("same_day"),
    OneDay("one_day"),
    OneWeek("one_week")
}

data class PlannedReminder(
    val event: EventDto,
    val kind: ReminderKind,
    val triggerAt: Instant
)

fun planReminders(
    events: List<EventDto>,
    settings: UserSettingsDto,
    now: Instant = Instant.now(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<PlannedReminder> {
    if (!settings.notifications_enabled) return emptyList()

    return events.flatMap { event ->
        val due = runCatching { OffsetDateTime.parse(event.due_date) }.getOrNull()
            ?: return@flatMap emptyList()
        if (event.reminder == "none") return@flatMap emptyList()

        val enabledKinds = buildSet {
            if (settings.reminder_same_day) add(ReminderKind.SameDay)
            if (settings.reminder_one_day) add(ReminderKind.OneDay)
            if (settings.reminder_one_week) add(ReminderKind.OneWeek)
            ReminderKind.entries.firstOrNull { it.storageValue == event.reminder }?.let(::add)
        }

        enabledKinds.mapNotNull { kind ->
            val trigger = when (kind) {
                ReminderKind.SameDay -> {
                    val dueLocal = due.atZoneSameInstant(zoneId)
                    val startOfDay = dueLocal.toLocalDate().atStartOfDay(zoneId)
                    maxOf(startOfDay, dueLocal.minusHours(2)).toInstant()
                }
                ReminderKind.OneDay -> due.minusDays(1).toInstant()
                ReminderKind.OneWeek -> due.minusWeeks(1).toInstant()
            }
            trigger.takeIf { it.isAfter(now) }?.let {
                PlannedReminder(event, kind, it)
            }
        }
    }.sortedBy { it.triggerAt }
}
