package com.example.miyad.ui.product

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyad.data.EventDto
import com.example.miyad.ui.components.GlassCard

private data class ChartSlice(
    val type: String,
    val color: Color,
    val count: Int,
)

@Composable
internal fun EventBreakdownChart(
    events: List<EventDto>,
    isArabic: Boolean,
) {
    val slices = listOf(
        ChartSlice("exam", Color(0xFFD94949), events.count { it.event_type == "exam" }),
        ChartSlice("deadline", Color(0xFFFFA000), events.count { it.event_type == "deadline" }),
        ChartSlice("quiz", Color(0xFF8E6AC8), events.count { it.event_type == "quiz" }),
        ChartSlice("lecture", Color(0xFF2388C9), events.count { it.event_type == "lecture" }),
        ChartSlice("other", Color(0xFF65A30D), events.count { it.event_type == "other" }),
    )
    val total = slices.sumOf { it.count }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    GlassCard(strong = true) {
        Text(
            text = if (isArabic) "توزيع المواعيد" else "Event breakdown",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                modifier = Modifier.size(132.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(122.dp)) {
                    val stroke = 15.dp.toPx()
                    val inset = stroke / 2
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                    if (total > 0) {
                        var startAngle = -90f
                        slices.filter { it.count > 0 }.forEach { slice ->
                            val sweep = 360f * slice.count / total
                            drawArc(
                                color = slice.color,
                                startAngle = startAngle,
                                sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - stroke, size.height - stroke),
                                style = Stroke(stroke, cap = StrokeCap.Round),
                            )
                            startAngle += sweep
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(total.toString(), fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text(
                        if (isArabic) "موعد" else "events",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                slices.forEach { slice ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(9.dp).clip(CircleShape).background(slice.color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            typeLabelForChart(slice.type, isArabic),
                            Modifier.weight(1f),
                            fontSize = 12.sp,
                        )
                        Text(slice.count.toString(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

private fun typeLabelForChart(type: String, arabic: Boolean): String = when (type) {
    "exam" -> if (arabic) "اختبارات" else "Exams"
    "deadline" -> if (arabic) "تسليمات" else "Deadlines"
    "quiz" -> if (arabic) "اختبارات قصيرة" else "Quizzes"
    "lecture" -> if (arabic) "محاضرات" else "Lectures"
    else -> if (arabic) "أخرى" else "Other"
}
