package com.example.miyad.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.miyad.data.EventDto
import com.example.miyad.data.UserSettingsDto
import com.google.gson.Gson
import java.time.Instant

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun sync(
        events: List<EventDto>,
        settings: UserSettingsDto,
        language: String,
        now: Instant = Instant.now()
    ) {
        cancelScheduledAlarms()
        persist(events, settings, language)
        planReminders(events, settings, now).forEach { schedule(it, language) }
    }

    fun restore() {
        val payload = preferences.getString(KEY_PAYLOAD, null) ?: return
        val stored = runCatching { gson.fromJson(payload, StoredSchedule::class.java) }
            .getOrNull() ?: return
        sync(stored.events, stored.settings, stored.language)
    }

    fun cancelAll() {
        cancelScheduledAlarms()
        preferences.edit().remove(KEY_PAYLOAD).remove(KEY_REQUEST_CODES).apply()
    }

    private fun schedule(reminder: PlannedReminder, language: String) {
        val requestCode = requestCode(reminder.event.id, reminder.kind)
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, reminder.event.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, reminder.event.title)
            putExtra(ReminderReceiver.EXTRA_DUE_DATE, reminder.event.due_date)
            putExtra(ReminderReceiver.EXTRA_EVENT_TYPE, reminder.event.event_type)
            putExtra(ReminderReceiver.EXTRA_KIND, reminder.kind.storageValue)
            putExtra(ReminderReceiver.EXTRA_LANGUAGE, language)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.triggerAt.toEpochMilli(),
            pendingIntent
        )
        rememberRequestCode(requestCode)
    }

    private fun cancelScheduledAlarms() {
        val requestCodes = preferences.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty()
        requestCodes.forEach { value ->
            val requestCode = value.toIntOrNull() ?: return@forEach
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        preferences.edit().remove(KEY_REQUEST_CODES).apply()
    }

    private fun rememberRequestCode(requestCode: Int) {
        val codes = preferences.getStringSet(KEY_REQUEST_CODES, emptySet())
            .orEmpty()
            .toMutableSet()
        codes += requestCode.toString()
        preferences.edit().putStringSet(KEY_REQUEST_CODES, codes).apply()
    }

    private fun persist(
        events: List<EventDto>,
        settings: UserSettingsDto,
        language: String
    ) {
        preferences.edit()
            .putString(KEY_PAYLOAD, gson.toJson(StoredSchedule(events, settings, language)))
            .apply()
    }

    private fun requestCode(eventId: String, kind: ReminderKind): Int =
        "$eventId:${kind.storageValue}".hashCode() and Int.MAX_VALUE

    private data class StoredSchedule(
        val events: List<EventDto>,
        val settings: UserSettingsDto,
        val language: String
    )

    companion object {
        private const val STORAGE_NAME = "miyad_reminder_schedule"
        private const val KEY_PAYLOAD = "payload"
        private const val KEY_REQUEST_CODES = "request_codes"
    }
}
