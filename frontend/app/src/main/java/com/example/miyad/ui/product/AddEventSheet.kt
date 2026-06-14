package com.example.miyad.ui.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyad.data.EventCreateRequest
import com.example.miyad.theme.MiyadDarkBackground
import com.example.miyad.theme.MiyadLime
import com.example.miyad.theme.ThmanyahSerifDisplay
import com.example.miyad.ui.components.GlassCard
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEventSheetV2(
    isArabic: Boolean,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSave: (EventCreateRequest) -> Unit,
) {
    val now = remember { LocalDateTime.now().withSecond(0).withNano(0) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(now.toLocalDate()) }
    var startTime by remember { mutableStateOf(now.toLocalTime()) }
    var endTime by remember { mutableStateOf(now.plusHours(1).toLocalTime()) }
    var allDay by remember { mutableStateOf(false) }
    var repeat by remember { mutableStateOf("none") }
    var location by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf("one_day") }
    var color by remember { mutableStateOf("#B8F23A") }
    var error by remember { mutableStateOf<String?>(null) }
    var optionalExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val locale = if (isArabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    }
    val timeFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(if (isArabic) "اختيار" else "Select") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            },
        ) { DatePicker(state = pickerState) }
    }

    if (showStartPicker) {
        TimePickerDialogContent(
            isArabic = isArabic,
            initialTime = startTime,
            onDismiss = { showStartPicker = false },
            onConfirm = {
                startTime = it
                if (!endTime.isAfter(it)) endTime = it.plusHours(1)
                showStartPicker = false
            },
        )
    }
    if (showEndPicker) {
        TimePickerDialogContent(
            isArabic = isArabic,
            initialTime = endTime,
            onDismiss = { showEndPicker = false },
            onConfirm = {
                endTime = it
                showEndPicker = false
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = if (isArabic) "إضافة موعد" else "Add event",
                    fontFamily = ThmanyahSerifDisplay,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                )
                Text(
                    text = if (isArabic) "أضف الأساسيات الآن، والتفاصيل عند الحاجة" else
                        "Start with the essentials and add details when needed",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إغلاق" else "Close")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassCard(strong = true) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isArabic) "العنوان" else "Title") },
                    singleLine = true,
                    isError = error != null && title.isBlank(),
                    shape = RoundedCornerShape(18.dp),
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(Icons.Default.CalendarMonth, null)
                    Spacer(Modifier.width(10.dp))
                    Text(date.format(dateFormatter), Modifier.weight(1f))
                }
                if (!allDay) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TimeButton(
                            label = if (isArabic) "البداية" else "Start",
                            value = startTime.format(timeFormatter),
                            modifier = Modifier.weight(1f),
                            onClick = { showStartPicker = true },
                        )
                        TimeButton(
                            label = if (isArabic) "النهاية" else "End",
                            value = endTime.format(timeFormatter),
                            modifier = Modifier.weight(1f),
                            onClick = { showEndPicker = true },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(if (isArabic) "طوال اليوم" else "All day", Modifier.weight(1f))
                    Switch(checked = allDay, onCheckedChange = { allDay = it })
                }
            }

            OutlinedButton(
                onClick = { optionalExpanded = !optionalExpanded },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    if (isArabic) "تفاصيل اختيارية" else "Optional details",
                    Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    if (optionalExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    null,
                )
            }

            AnimatedVisibility(visible = optionalExpanded) {
                GlassCard {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isArabic) "الوصف" else "Description") },
                        minLines = 2,
                        shape = RoundedCornerShape(18.dp),
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isArabic) "المكان أو الرابط" else "Location or link") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                    )
                    ChoiceRow(
                        label = if (isArabic) "التكرار" else "Repeat",
                        values = listOf("none", "daily", "weekly", "monthly", "custom"),
                        selected = repeat,
                        display = { repeatLabelV2(it, isArabic) },
                        onSelected = { repeat = it },
                    )
                    ChoiceRow(
                        label = if (isArabic) "التذكير" else "Reminder",
                        values = listOf("same_day", "one_day", "one_week", "none"),
                        selected = reminder,
                        display = { reminderLabelV2(it, isArabic) },
                        onSelected = { reminder = it },
                    )
                    Text(
                        if (isArabic) "لون الموعد" else "Event color",
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("#B8F23A", "#2388C9", "#8E6AC8", "#FFA000", "#D94949")
                            .forEach { value ->
                                val selected = color == value
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(value)))
                                        .border(
                                            if (selected) 3.dp else 0.dp,
                                            androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                            CircleShape,
                                        )
                                        .clickable { color = value }
                                )
                            }
                    }
                }
            }
            error?.let {
                Text(
                    text = it,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                )
            }
        }

        Button(
            onClick = {
                val request = runCatching {
                    buildEventCreateRequest(
                        EventFormValues(
                            title = title,
                            description = description,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            allDay = allDay,
                            repeat = repeat,
                            location = location,
                            reminder = reminder,
                            color = color,
                        )
                    )
                }.getOrElse {
                    error = if (isArabic) {
                        "أدخل عنوانًا وتأكد أن وقت النهاية بعد البداية"
                    } else {
                        "Add a title and make sure the end time follows the start"
                    }
                    return@Button
                }
                error = null
                onSave(request)
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MiyadLime,
                contentColor = MiyadDarkBackground,
            ),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    Modifier.size(22.dp),
                    color = MiyadDarkBackground,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isArabic) "حفظ الموعد" else "Save event",
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogContent(
    isArabic: Boolean,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
    )
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(if (isArabic) "اختيار" else "Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        },
        text = { TimePicker(state = state) },
    )
}

@Composable
private fun TimeButton(
    label: String,
    value: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Icon(Icons.Default.Schedule, null)
        Spacer(Modifier.width(7.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(label, fontSize = 10.sp)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    values: List<String>,
    selected: String,
    display: (String) -> String,
    onSelected: (String) -> Unit,
) {
    Text(label, fontWeight = FontWeight.Bold)
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        values.forEach { value ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelected(value) },
                label = { Text(display(value)) },
            )
        }
    }
}

private fun repeatLabelV2(value: String, arabic: Boolean): String = when (value) {
    "daily" -> if (arabic) "يومي" else "Daily"
    "weekly" -> if (arabic) "أسبوعي" else "Weekly"
    "monthly" -> if (arabic) "شهري" else "Monthly"
    "custom" -> if (arabic) "مخصص" else "Custom"
    else -> if (arabic) "بدون" else "None"
}

private fun reminderLabelV2(value: String, arabic: Boolean): String = when (value) {
    "same_day" -> if (arabic) "نفس اليوم" else "Same day"
    "one_day" -> if (arabic) "قبل يوم" else "One day"
    "one_week" -> if (arabic) "قبل أسبوع" else "One week"
    else -> if (arabic) "بدون" else "None"
}
