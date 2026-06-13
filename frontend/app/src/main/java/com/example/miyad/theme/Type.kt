package com.example.miyad.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.miyad.R

// ─── عائلات خط ثمانية ───────────────────────────────────────────────────────

/**
 * Thmanyah Sans — للأزرار والتسميات والعناصر التفاعلية
 * يمتد من Thin إلى Black لإضفاء وزن بصري واضح
 */
val ThmanyahSans = FontFamily(
    Font(R.font.thmanyahsans_light,   FontWeight.Light),
    Font(R.font.thmanyahsans_regular, FontWeight.Normal),
    Font(R.font.thmanyahsans_medium,  FontWeight.Medium),
    Font(R.font.thmanyahsans_bold,    FontWeight.Bold),
    Font(R.font.thmanyahsans_black,   FontWeight.Black)
)

/**
 * Thmanyah Serif Display — للعناوين الرئيسية والـ hero text
 * خط serif أنيق يُضفي طابعاً تحريرياً راقياً
 */
val ThmanyahSerifDisplay = FontFamily(
    Font(R.font.thmanyahserifdisplay_light,   FontWeight.Light),
    Font(R.font.thmanyahserifdisplay_regular, FontWeight.Normal),
    Font(R.font.thmanyahserifdisplay_medium,  FontWeight.Medium),
    Font(R.font.thmanyahserifdisplay_bold,    FontWeight.Bold),
    Font(R.font.thmanyahserifdisplay_black,   FontWeight.Black)
)

/**
 * Thmanyah Serif Text — للنصوص الطويلة والأوصاف
 * خط serif مُحسَّن للقراءة في الأحجام الصغيرة
 */
val ThmanyahSerifText = FontFamily(
    Font(R.font.thmanyahseriftext_light,   FontWeight.Light),
    Font(R.font.thmanyahseriftext_regular, FontWeight.Normal),
    Font(R.font.thmanyahseriftext_medium,  FontWeight.Medium),
    Font(R.font.thmanyahseriftext_bold,    FontWeight.Bold),
    Font(R.font.thmanyahseriftext_black,   FontWeight.Black)
)

// ─── نظام الطباعة ──────────────────────────────────────────────────────────

val Typography = Typography(

    // ── Display: عنوان بطولي ضخم (شاشة Onboarding / صفحات الترحيب)
    displayLarge = TextStyle(
        fontFamily = ThmanyahSerifDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = ThmanyahSerifDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontFamily = ThmanyahSerifDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    // ── Headlines: عناوين الصفحات والبطاقات
    headlineLarge = TextStyle(
        fontFamily = ThmanyahSerifDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ThmanyahSerifDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ThmanyahSerifDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // ── Titles: عناوين البطاقات والأقسام
    titleLarge = TextStyle(
        fontFamily = ThmanyahSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ThmanyahSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ThmanyahSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Body: النصوص الطويلة والأوصاف
    bodyLarge = TextStyle(
        fontFamily = ThmanyahSerifText,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ThmanyahSerifText,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ThmanyahSerifText,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),

    // ── Labels: التسميات والأزرار والعلامات
    labelLarge = TextStyle(
        fontFamily = ThmanyahSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ThmanyahSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ThmanyahSans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
