package com.example.miyad.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.miyad.MainActivity
import com.example.miyad.R
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        createChannel(context)
        val language = intent.getStringExtra(EXTRA_LANGUAGE) ?: "ar"
        val isArabic = language == "ar"
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val dueDate = formatDueDate(intent.getStringExtra(EXTRA_DUE_DATE), isArabic)
        val kind = intent.getStringExtra(EXTRA_KIND)
        val reminderLabel = when (kind) {
            ReminderKind.OneWeek.storageValue -> if (isArabic) "متبقٍ أسبوع" else "One week remaining"
            ReminderKind.OneDay.storageValue -> if (isArabic) "متبقٍ يوم واحد" else "One day remaining"
            else -> if (isArabic) "موعدك اليوم" else "Your event is today"
        }

        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("$reminderLabel · $dueDate")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$reminderLabel\n$dueDate")
            )
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID).orEmpty()
        NotificationManagerCompat.from(context)
            .notify(eventId.hashCode() and Int.MAX_VALUE, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Academic reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Upcoming Miyad exams, deadlines, quizzes, and lectures"
            }
        )
    }

    private fun formatDueDate(value: String?, isArabic: Boolean): String {
        if (value == null) return ""
        return runCatching {
            val locale = if (isArabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
            OffsetDateTime.parse(value).format(
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withLocale(locale)
            )
        }.getOrDefault(value)
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DUE_DATE = "due_date"
        const val EXTRA_EVENT_TYPE = "event_type"
        const val EXTRA_KIND = "kind"
        const val EXTRA_LANGUAGE = "language"
        private const val CHANNEL_ID = "miyad_academic_reminders"
    }
}
