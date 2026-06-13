package com.example.miyad.notifications

import com.example.miyad.data.EventDto
import com.example.miyad.data.UserSettingsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ReminderPlannerTest {
    private val now = Instant.parse("2026-06-01T00:00:00Z")

    @Test
    fun enabledGlobalAndEventSpecificRemindersArePlanned() {
        val settings = UserSettingsDto(
            reminder_same_day = true,
            reminder_one_day = false,
            reminder_one_week = false
        )
        val event = event(reminder = "one_week")

        val reminders = planReminders(
            listOf(event),
            settings,
            now,
            ZoneId.of("Asia/Singapore")
        )

        assertEquals(
            setOf(ReminderKind.SameDay, ReminderKind.OneWeek),
            reminders.map { it.kind }.toSet()
        )
    }

    @Test
    fun disabledNotificationsAndNoneOverrideProduceNoAlarms() {
        assertTrue(
            planReminders(
                listOf(event()),
                UserSettingsDto(notifications_enabled = false),
                now
            ).isEmpty()
        )
        assertTrue(
            planReminders(
                listOf(event(reminder = "none")),
                UserSettingsDto(),
                now
            ).isEmpty()
        )
    }

    private fun event(reminder: String? = null) = EventDto(
        id = "event-1",
        title = "Database deadline",
        course_code = "DB201",
        event_type = "deadline",
        due_date = "2026-06-20T14:00:00+08:00",
        location = "LMS",
        notes = null,
        reminder = reminder
    )
}
