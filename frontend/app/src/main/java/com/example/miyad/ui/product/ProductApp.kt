package com.example.miyad.ui.product

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miyad.BuildConfig
import com.example.miyad.data.EventDto
import com.example.miyad.data.UserSettingsUpdate
import com.example.miyad.theme.MiyadDarkBackground
import com.example.miyad.theme.MiyadLime
import com.example.miyad.theme.MiyadPageBackground
import com.example.miyad.theme.MiyadTextSecondary
import com.example.miyad.theme.ThmanyahSans
import com.example.miyad.theme.ThmanyahSerifDisplay
import com.example.miyad.ui.AppUiState
import com.example.miyad.ui.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private enum class AppScreen { Splash, Onboarding, Language, Auth, Home, Calendar, Extract, Settings, Details }

@Composable
fun ProductApp(viewModel: AppViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var screen by remember { mutableStateOf(AppScreen.Splash) }
    var detailsReturnScreen by remember { mutableStateOf(AppScreen.Home) }
    val isArabic = state.language == "ar"
    val direction = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.initialized) {
        if (!state.initialized) return@LaunchedEffect
        delay(1200)
        screen = when {
            !state.onboardingComplete -> AppScreen.Onboarding
            !state.authenticated -> AppScreen.Auth
            else -> AppScreen.Home
        }
    }
    LaunchedEffect(state.authenticated) {
        if (state.authenticated && screen == AppScreen.Auth) screen = AppScreen.Home
        if (!state.authenticated && screen !in listOf(
                AppScreen.Splash, AppScreen.Onboarding, AppScreen.Language, AppScreen.Auth
            )
        ) screen = AppScreen.Auth
    }
    LaunchedEffect(state.error, state.message) {
        val feedback = state.error ?: state.message
        if (feedback != null) {
            snackbar.showSnackbar(feedback)
            viewModel.clearFeedback()
        }
    }
    BackHandler(enabled = screen == AppScreen.Details) {
        screen = detailsReturnScreen
    }

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Box(Modifier.fillMaxSize().background(MiyadPageBackground)) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "screen"
            ) { current ->
                when (current) {
                    AppScreen.Splash -> SplashScreen()
                    AppScreen.Onboarding -> OnboardingScreen(
                        isArabic = isArabic,
                        onContinue = { screen = AppScreen.Language }
                    )
                    AppScreen.Language -> LanguageSelectionScreen(
                        onSelect = {
                            viewModel.setLanguage(it)
                            viewModel.completeOnboarding()
                            screen = AppScreen.Auth
                        }
                    )
                    AppScreen.Auth -> AuthenticationScreen(
                        state = state,
                        isArabic = isArabic,
                        onLogin = viewModel::login,
                        onRegister = viewModel::register
                    )
                    AppScreen.Home -> MainShell(
                        selected = AppScreen.Home,
                        state = state,
                        isArabic = isArabic,
                        onNavigate = { screen = it },
                        onRefresh = viewModel::refresh,
                        onEvent = {
                            viewModel.selectEvent(it)
                            detailsReturnScreen = AppScreen.Home
                            screen = AppScreen.Details
                        }
                    )
                    AppScreen.Calendar -> MainShell(
                        selected = AppScreen.Calendar,
                        state = state,
                        isArabic = isArabic,
                        onNavigate = { screen = it },
                        onRefresh = viewModel::refresh,
                        onEvent = {
                            viewModel.selectEvent(it)
                            detailsReturnScreen = AppScreen.Calendar
                            screen = AppScreen.Details
                        }
                    )
                    AppScreen.Extract -> MainShell(
                        selected = AppScreen.Extract,
                        state = state,
                        isArabic = isArabic,
                        onNavigate = { screen = it },
                        onRefresh = viewModel::refresh,
                        onEvent = {}
                    )
                    AppScreen.Settings -> MainShell(
                        selected = AppScreen.Settings,
                        state = state,
                        isArabic = isArabic,
                        onNavigate = { screen = it },
                        onRefresh = viewModel::refresh,
                        onEvent = {}
                    )
                    AppScreen.Details -> EventDetailsScreen(
                        event = state.selectedEvent,
                        isArabic = isArabic,
                        loading = state.loading,
                        onBack = { screen = detailsReturnScreen },
                        onReminder = viewModel::updateSelectedReminder,
                        onDelete = {
                            viewModel.deleteSelectedEvent()
                            screen = detailsReturnScreen
                        }
                    )
                }
            }
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
            )
        }
    }
}

@Composable
fun SplashScreen() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(if (visible) 1f else .72f, tween(700), label = "logo")
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MiyadDarkBackground, Color(0xFF1B2718)))
        ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible, enter = fadeIn(tween(650)) + scaleIn(tween(650))) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(96.dp).scale(scale).clip(RoundedCornerShape(28.dp))
                        .background(MiyadLime),
                    contentAlignment = Alignment.Center
                ) {
                    Text("م", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 55.sp, color = MiyadDarkBackground)
                }
                Spacer(Modifier.height(20.dp))
                Text("مِيعاد", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 44.sp, color = Color.White)
                Text("MIYAD", letterSpacing = 4.sp, fontFamily = ThmanyahSans, fontSize = 11.sp, color = MiyadLime)
            }
        }
    }
}

@Composable
fun OnboardingScreen(isArabic: Boolean, onContinue: () -> Unit) {
    val pages = if (isArabic) listOf(
        Triple("التقط مواعيدك", "افتح رسالة الجامعة في Outlook، واترك مِيعاد يلتقط الموعد.", Icons.Default.Email),
        Triple("استخلاص ذكي", "يحوّل الذكاء الاصطناعي الرسائل العربية والإنجليزية إلى أحداث واضحة.", Icons.Default.AutoAwesome),
        Triple("تقويم وتذكيرات", "شاهد الاختبارات والتسليمات والمحاضرات في مكان واحد.", Icons.Default.CalendarMonth),
        Triple("خصوصيتك أولاً", "لا نطلب كلمة مرور الجامعة، ولا نخزّن نص الرسالة.", Icons.Default.Security)
    ) else listOf(
        Triple("Capture deadlines", "Open a university email in Outlook and Miyad captures the event.", Icons.Default.Email),
        Triple("Smart extraction", "AI turns Arabic and English messages into clear academic events.", Icons.Default.AutoAwesome),
        Triple("Calendar and reminders", "See exams, deadlines, quizzes and lectures in one place.", Icons.Default.CalendarMonth),
        Triple("Privacy first", "No university password is requested and raw email text is never stored.", Icons.Default.Security)
    )
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().background(MiyadPageBackground).statusBarsPadding()
            .navigationBarsPadding().padding(24.dp)
    ) {
        Text("مِيعاد", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 28.sp)
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val item = pages[page]
            Column(
                Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(150.dp).clip(RoundedCornerShape(45.dp))
                        .background(Brush.linearGradient(listOf(MiyadLime, Color(0xFFEAFBC5)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.third, null, Modifier.size(72.dp), tint = MiyadDarkBackground)
                }
                Spacer(Modifier.height(34.dp))
                Text(item.first, fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 30.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(item.second, fontFamily = ThmanyahSans, color = MiyadTextSecondary, textAlign = TextAlign.Center, lineHeight = 25.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            pages.indices.forEach { index ->
                Box(
                    Modifier.padding(4.dp).height(8.dp)
                        .width(if (index == pagerState.currentPage) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (index == pagerState.currentPage) MiyadDarkBackground else Color(0xFFD9DDD5))
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                if (pagerState.currentPage == pages.lastIndex) onContinue()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MiyadDarkBackground,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                if (pagerState.currentPage == pages.lastIndex) {
                    if (isArabic) "اختيار اللغة" else "Choose language"
                } else if (isArabic) "التالي" else "Next",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LanguageSelectionScreen(onSelect: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Language, null, Modifier.size(54.dp), tint = MiyadDarkBackground)
        Spacer(Modifier.height(22.dp))
        Text("اختر لغتك", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 34.sp)
        Text("Choose your language", fontFamily = ThmanyahSans, color = MiyadTextSecondary, fontSize = 17.sp)
        Spacer(Modifier.height(34.dp))
        LanguageCard("العربية", "واجهة عربية من اليمين إلى اليسار", "ع", true) { onSelect("ar") }
        Spacer(Modifier.height(14.dp))
        LanguageCard("English", "English left-to-right interface", "EN", false) { onSelect("en") }
    }
}

@Composable
private fun LanguageCard(title: String, subtitle: String, mark: String, highlighted: Boolean, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlighted) Color(0xFFEAFBC5) else Color.White)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(if (highlighted) MiyadLime else Color(0xFFF0F2ED)), contentAlignment = Alignment.Center) {
                Text(mark, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontFamily = ThmanyahSans, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, fontFamily = ThmanyahSans, color = MiyadTextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AuthenticationScreen(
    state: AppUiState,
    isArabic: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit
) {
    var register by remember { mutableStateOf(false) }
    AnimatedContent(register, label = "auth-mode") { registering ->
        if (registering) RegisterScreen(state.loading, isArabic, onRegister, onLoginClick = { register = false })
        else LoginScreen(state.loading, isArabic, onLogin, onRegisterClick = { register = true })
    }
}

@Composable
fun LoginScreen(loading: Boolean, isArabic: Boolean, onLogin: (String, String) -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    AuthLayout(
        title = if (isArabic) "مرحباً بعودتك" else "Welcome back",
        subtitle = if (isArabic) "سجّل الدخول لمزامنة مواعيدك" else "Sign in to sync your academic schedule"
    ) {
        AuthField(email, { email = it }, if (isArabic) "البريد الإلكتروني" else "Email")
        AuthField(password, { password = it }, if (isArabic) "كلمة المرور" else "Password", password = true)
        PrimaryButton(if (isArabic) "تسجيل الدخول" else "Sign in", loading) { onLogin(email, password) }
        TextButton(onClick = onRegisterClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (isArabic) "ليس لديك حساب؟ أنشئ حساباً" else "New to Miyad? Create an account")
        }
    }
}

@Composable
fun RegisterScreen(
    loading: Boolean,
    isArabic: Boolean,
    onRegister: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    AuthLayout(
        title = if (isArabic) "حساب مِيعاد جديد" else "Create your Miyad account",
        subtitle = if (isArabic) "ابدأ بتنظيم فصلك الأكاديمي" else "Start organizing your academic semester"
    ) {
        AuthField(name, { name = it }, if (isArabic) "الاسم" else "Name")
        AuthField(university, { university = it }, if (isArabic) "الجامعة" else "University")
        AuthField(email, { email = it }, if (isArabic) "البريد الإلكتروني" else "Email")
        AuthField(password, { password = it }, if (isArabic) "كلمة المرور (8 أحرف على الأقل)" else "Password (8+ characters)", password = true)
        PrimaryButton(if (isArabic) "إنشاء الحساب" else "Create account", loading) {
            onRegister(email, password, name, university)
        }
        TextButton(onClick = onLoginClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (isArabic) "لديك حساب؟ سجّل الدخول" else "Already have an account? Sign in")
        }
    }
}

@Composable
private fun AuthLayout(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()
            .navigationBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(MiyadLime), contentAlignment = Alignment.Center) {
            Text("م", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 34.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(title, fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 31.sp)
        Text(subtitle, fontFamily = ThmanyahSans, color = MiyadTextSecondary)
        Spacer(Modifier.height(25.dp))
        content()
    }
}

@Composable
private fun AuthField(value: String, onValue: (String) -> Unit, label: String, password: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
    )
}

@Composable
private fun PrimaryButton(label: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(55.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MiyadDarkBackground,
            contentColor = Color.White
        )
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = MiyadLime, strokeWidth = 2.dp)
        else Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MainShell(
    selected: AppScreen,
    state: AppUiState,
    isArabic: Boolean,
    onNavigate: (AppScreen) -> Unit,
    onRefresh: () -> Unit,
    onEvent: (EventDto) -> Unit
) {
    Scaffold(
        containerColor = MiyadPageBackground,
        bottomBar = {
            NavigationBar(containerColor = MiyadDarkBackground, modifier = Modifier.navigationBarsPadding()) {
                listOf(
                    Triple(AppScreen.Home, Icons.Default.Home, if (isArabic) "الرئيسية" else "Home"),
                    Triple(AppScreen.Calendar, Icons.Default.CalendarMonth, if (isArabic) "التقويم" else "Calendar"),
                    Triple(AppScreen.Extract, Icons.Default.AutoAwesome, if (isArabic) "استخلاص" else "Extract"),
                    Triple(AppScreen.Settings, Icons.Default.Settings, if (isArabic) "الإعدادات" else "Settings")
                ).forEach { item ->
                    NavigationBarItem(
                        selected = selected == item.first,
                        onClick = { onNavigate(item.first) },
                        icon = { Icon(item.second, item.third) },
                        label = { Text(item.third, fontSize = 10.sp) },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = MiyadDarkBackground,
                            selectedTextColor = MiyadLime,
                            indicatorColor = MiyadLime,
                            unselectedIconColor = Color(0xFF9DA79A),
                            unselectedTextColor = Color(0xFF9DA79A)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                AppScreen.Home -> HomeDashboardScreen(state, isArabic, onRefresh, onEvent)
                AppScreen.Calendar -> CalendarScreen(state, isArabic, onRefresh, onEvent)
                AppScreen.Extract -> SmartExtractionScreen(state, isArabic)
                AppScreen.Settings -> ProfileSettingsScreen(state, isArabic)
                else -> Unit
            }
        }
    }
}

@Composable
fun HomeDashboardScreen(state: AppUiState, isArabic: Boolean, onRefresh: () -> Unit, onEvent: (EventDto) -> Unit) {
    val metrics = dashboardMetrics(state.events)
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (isArabic) "مرحباً ${state.user?.name ?: ""}" else "Hello ${state.user?.name ?: ""}", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 28.sp)
                    Text(if (isArabic) "هذه نظرة على أسبوعك الأكاديمي" else "Here is your academic week", color = MiyadTextSecondary)
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, if (isArabic) "تحديث" else "Refresh") }
            }
        }
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MiyadDarkBackground)) {
                Column(Modifier.padding(22.dp)) {
                    Text(if (isArabic) "الموعد القادم" else "Next deadline", color = MiyadLime, fontWeight = FontWeight.Bold)
                    val next = state.events.firstOrNull()
                    Text(next?.title ?: if (isArabic) "لا توجد مواعيد قادمة" else "No upcoming events", color = Color.White, fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Bold, fontSize = 23.sp, modifier = Modifier.padding(top = 8.dp))
                    if (next != null) Text(formatDate(next.due_date, isArabic), color = Color(0xFFBBC4B8), modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    if (isArabic) "مواعيد اليوم" else "Today",
                    metrics.todayCount.toString(),
                    Modifier.weight(1f)
                )
                SummaryCard(
                    if (isArabic) "هذا الأسبوع" else "This week",
                    metrics.weekCount.toString(),
                    Modifier.weight(1f)
                )
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.fillMaxWidth().padding(17.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (isArabic) "تقدم الأسبوع" else "Weekly progress",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${(metrics.weekProgress * 100).toInt()}%",
                            color = MiyadTextSecondary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { metrics.weekProgress },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(7.dp)
                            .clip(CircleShape),
                        color = MiyadLime,
                        trackColor = Color(0xFFE8EBE5)
                    )
                }
            }
        }
        item {
            val types = listOf("exam", "deadline", "quiz", "lecture", "other")
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { type ->
                    CounterCard(
                        typeLabel(type, isArabic),
                        state.events.count { it.event_type == type },
                        Modifier.width(92.dp)
                    )
                }
            }
        }
        item {
            Text(if (isArabic) "النشاط الأكاديمي القادم" else "Upcoming academic activity", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        when {
            state.loading && state.events.isEmpty() -> items(3) { SkeletonCard() }
            state.error != null && state.events.isEmpty() -> item { ErrorState(state.error, isArabic, onRefresh) }
            state.events.isEmpty() -> item { EmptyState(isArabic) }
            else -> itemsIndexed(
                state.events.take(6),
                key = { _, event -> event.id }
            ) { index, event ->
                AnimatedEventCard(event, isArabic, index) { onEvent(event) }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAFBC5))
    ) {
        Column(Modifier.fillMaxWidth().padding(17.dp)) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 25.sp)
            Text(label, color = MiyadTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CounterCard(label: String, count: Int, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(vertical = 14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(label, color = MiyadTextSecondary, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun AnimatedEventCard(
    event: EventDto,
    isArabic: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    var visible by remember(event.id) { mutableStateOf(false) }
    LaunchedEffect(event.id) {
        delay(index.coerceAtMost(5) * 45L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + slideInVertically(tween(240)) { it / 5 }
    ) {
        EventCard(event, isArabic, onClick)
    }
}

@Composable
fun CalendarScreen(state: AppUiState, isArabic: Boolean, onRefresh: () -> Unit, onEvent: (EventDto) -> Unit) {
    var filter by remember { mutableStateOf<String?>(null) }
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val locale = if (isArabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
    val monthDays = monthCalendarDays(displayedMonth)
    val eventDates = state.events.mapNotNull { it.localDateOrNull() }.toSet()
    val visibleEvents = eventsForDate(state.events, selectedDate, filter)

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isArabic) "التقويم" else "Calendar", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 29.sp)
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, null) }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            displayedMonth = displayedMonth.minusMonths(1)
                            selectedDate = displayedMonth.atDay(1)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                if (isArabic) "الشهر السابق" else "Previous month"
                            )
                        }
                        AnimatedContent(displayedMonth, label = "calendar-month") { month ->
                            Text(
                                month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)),
                                fontFamily = ThmanyahSerifDisplay,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        }
                        IconButton(onClick = {
                            displayedMonth = displayedMonth.plusMonths(1)
                            selectedDate = displayedMonth.atDay(1)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                if (isArabic) "الشهر التالي" else "Next month"
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth()) {
                        monthDays.take(7).forEach { day ->
                            Text(
                                day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                color = MiyadTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    monthDays.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                val isSelected = day.date == selectedDate
                                val isToday = day.date == LocalDate.now()
                                val hasEvent = day.date in eventDates
                                val dayScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.08f else 1f,
                                    animationSpec = tween(160),
                                    label = "calendar-day-scale"
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedDate = day.date
                                            if (!day.isInDisplayedMonth) {
                                                displayedMonth = YearMonth.from(day.date)
                                            }
                                        }
                                        .padding(vertical = 5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        Modifier.size(34.dp).scale(dayScale).clip(CircleShape).background(
                                            when {
                                                isSelected -> MiyadDarkBackground
                                                hasEvent -> MiyadLime
                                                isToday -> Color(0xFFEAFBC5)
                                                else -> Color.Transparent
                                            }
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            day.date.dayOfMonth.toString(),
                                            color = when {
                                                isSelected -> Color.White
                                                !day.isInDisplayedMonth -> MiyadTextSecondary.copy(alpha = .42f)
                                                else -> Color.Unspecified
                                            },
                                            fontWeight = if (hasEvent || isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    Box(
                                        Modifier.padding(top = 3.dp).size(4.dp).clip(CircleShape)
                                            .background(if (hasEvent) eventColorForDate(state.events, day.date) else Color.Transparent)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedDate.format(
                        DateTimeFormatter.ofPattern(
                            if (isArabic) "EEEE، d MMMM" else "EEEE, MMMM d",
                            locale
                        )
                    ),
                    fontFamily = ThmanyahSerifDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (selectedDate != LocalDate.now()) {
                    TextButton(onClick = {
                        selectedDate = LocalDate.now()
                        displayedMonth = YearMonth.now()
                    }) {
                        Text(if (isArabic) "اليوم" else "Today")
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf<String?>(null, "exam", "deadline", "quiz", "lecture", "other").forEach { type ->
                    FilterChip(
                        selected = filter == type,
                        onClick = { filter = type },
                        label = { Text(type?.let { typeLabel(it, isArabic) } ?: if (isArabic) "الكل" else "All") }
                    )
                }
            }
        }
        if (visibleEvents.isEmpty()) item { EmptyState(isArabic) }
        else itemsIndexed(
            visibleEvents,
            key = { _, event -> event.id }
        ) { index, event ->
            AnimatedEventCard(event, isArabic, index) { onEvent(event) }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

private fun eventColorForDate(events: List<EventDto>, date: LocalDate): Color {
    val highestPriority = eventsForDate(events, date)
        .minByOrNull {
            when (it.event_type) {
                "exam" -> 0
                "deadline" -> 1
                "quiz" -> 2
                "lecture" -> 3
                else -> 4
            }
        }
    return highestPriority?.let { eventColor(it.event_type) } ?: Color.Transparent
}

@Composable
fun EventDetailsScreen(
    event: EventDto?,
    isArabic: Boolean,
    loading: Boolean,
    onBack: () -> Unit,
    onReminder: (String) -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(if (isArabic) "حذف الموعد؟" else "Delete event?") },
            text = { Text(if (isArabic) "لا يمكن التراجع عن هذا الإجراء." else "This action cannot be undone.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text(if (isArabic) "حذف" else "Delete", color = Color(0xFFD94949)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(if (isArabic) "إلغاء" else "Cancel") } }
        )
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()
            .navigationBarsPadding().padding(18.dp)
    ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
        if (event == null) {
            EmptyState(isArabic)
            return@Column
        }
        Spacer(Modifier.height(10.dp))
        Text(typeLabel(event.event_type, isArabic), color = MiyadTextSecondary, fontWeight = FontWeight.Bold)
        Text(event.title, fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 39.sp)
        event.course_code?.let { Text(it, color = MiyadTextSecondary, modifier = Modifier.padding(top = 5.dp)) }
        Spacer(Modifier.height(22.dp))
        DetailRow(Icons.Default.Schedule, if (isArabic) "التاريخ والوقت" else "Date and time", formatDate(event.due_date, isArabic))
        DetailRow(Icons.Default.LocationOn, if (isArabic) "المكان" else "Location", event.location ?: if (isArabic) "غير محدد" else "Not specified")
        DetailRow(Icons.Default.Email, if (isArabic) "المصدر" else "Source", if (event.source_hash != null) "Outlook / Miyad AI" else if (isArabic) "إدخال يدوي" else "Manual")
        event.notes?.let {
            Card(Modifier.fillMaxWidth().padding(top = 14.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp)) {
                    Text(if (isArabic) "ملاحظات" else "Notes", fontWeight = FontWeight.Bold)
                    Text(it, color = MiyadTextSecondary, modifier = Modifier.padding(top = 7.dp), lineHeight = 23.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            if (isArabic) "تذكير هذا الموعد" else "Event reminder",
            fontFamily = ThmanyahSerifDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            if (isArabic) {
                "يضاف هذا الخيار إلى إعدادات التذكير العامة."
            } else {
                "This option is added to your global reminder settings."
            },
            color = MiyadTextSecondary,
            fontSize = 12.sp
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf(
                "same_day" to if (isArabic) "نفس اليوم" else "Same day",
                "one_day" to if (isArabic) "قبل يوم" else "One day",
                "one_week" to if (isArabic) "قبل أسبوع" else "One week",
                "none" to if (isArabic) "بدون" else "None"
            ).forEach { (value, label) ->
                FilterChip(
                    selected = event.reminder == value,
                    onClick = { onReminder(value) },
                    enabled = !loading,
                    label = { Text(label) }
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        OutlinedButton(onClick = { confirmDelete = true }, enabled = !loading, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(17.dp)) {
            Icon(Icons.Default.Delete, null, tint = Color(0xFFD94949))
            Spacer(Modifier.width(8.dp))
            Text(if (isArabic) "حذف الموعد" else "Delete event", color = Color(0xFFD94949), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFEAFBC5)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MiyadDarkBackground)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = MiyadTextSecondary, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SmartExtractionScreen(state: AppUiState, isArabic: Boolean) {
    val viewModel: AppViewModel = viewModel()
    var text by remember { mutableStateOf("") }
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(if (isArabic) "الاستخلاص الذكي" else "Smart extraction", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 29.sp, modifier = Modifier.padding(top = 16.dp))
            Text(if (isArabic) "ألصق نص رسالة جامعية لمراجعة المواعيد قبل حفظها." else "Paste a university email and review events before saving.", color = MiyadTextSecondary)
        }
        item {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(210.dp),
                label = { Text(if (isArabic) "نص الرسالة" else "Email text") },
                shape = RoundedCornerShape(20.dp)
            )
        }
        item {
            Button(
                onClick = { viewModel.previewExtraction(text) },
                enabled = !state.extractionLoading,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiyadDarkBackground,
                    contentColor = Color.White
                )
            ) {
                if (state.extractionLoading) CircularProgressIndicator(Modifier.size(22.dp), color = MiyadLime, strokeWidth = 2.dp)
                else {
                    Icon(Icons.Default.AutoAwesome, null, tint = MiyadLime)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isArabic) "استخلاص بالذكاء الاصطناعي" else "Extract with AI", fontWeight = FontWeight.Bold)
                }
            }
        }
        if (state.extractionPreview.isNotEmpty()) {
            item { Text(if (isArabic) "مراجعة العناصر المستخرجة" else "Review extracted events", fontWeight = FontWeight.Bold, fontSize = 19.sp) }
            items(state.extractionPreview) { EventCard(it, isArabic, null) }
            item {
                Button(
                    onClick = { viewModel.saveExtraction(text) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MiyadLime, contentColor = MiyadDarkBackground)
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isArabic) "حفظ في التقويم" else "Save to calendar", fontWeight = FontWeight.Black)
                }
            }
        }
        item {
            AnimatedVisibility(
                visible = state.extractionSaved,
                enter = fadeIn(tween(240)) + scaleIn(tween(300))
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAFBC5))
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            Modifier.size(30.dp),
                            tint = MiyadDarkBackground
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (isArabic) "تمت إضافة المواعيد إلى تقويمك" else
                                "Events added to your calendar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
fun ProfileSettingsScreen(state: AppUiState, isArabic: Boolean) {
    val viewModel: AppViewModel = viewModel()
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text(if (isArabic) "الإعدادات" else "Settings", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 29.sp, modifier = Modifier.padding(top = 16.dp)) }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MiyadDarkBackground)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, null, Modifier.size(56.dp), tint = MiyadLime)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(state.user?.name ?: if (isArabic) "طالب مِيعاد" else "Miyad student", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(state.user?.email ?: "", color = Color(0xFFAEB7AB))
                        Text(state.user?.university ?: "", color = Color(0xFFAEB7AB), fontSize = 12.sp)
                    }
                }
            }
        }
        item {
            SettingsToggle(
                Icons.Default.Language,
                if (isArabic) "اللغة" else "Language",
                if (isArabic) "العربية" else "English",
                checked = isArabic,
                onChecked = { viewModel.setLanguage(if (it) "ar" else "en") }
            )
        }
        item {
            SettingsToggle(
                Icons.Default.Notifications,
                if (isArabic) "الإشعارات" else "Notifications",
                if (isArabic) "تنبيهات المواعيد الأكاديمية" else "Academic event alerts",
                state.settings.notifications_enabled
            ) { viewModel.updateSettings(UserSettingsUpdate(notifications_enabled = it)) }
        }
        item {
            Text(if (isArabic) "توقيت التذكير" else "Reminder timing", fontWeight = FontWeight.Bold)
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ReminderToggle(if (isArabic) "في نفس اليوم" else "Same day", state.settings.reminder_same_day) { viewModel.updateSettings(UserSettingsUpdate(reminder_same_day = it)) }
                    HorizontalDivider(color = Color(0xFFE8EBE5))
                    ReminderToggle(if (isArabic) "قبل يوم" else "One day before", state.settings.reminder_one_day) { viewModel.updateSettings(UserSettingsUpdate(reminder_one_day = it)) }
                    HorizontalDivider(color = Color(0xFFE8EBE5))
                    ReminderToggle(if (isArabic) "قبل أسبوع" else "One week before", state.settings.reminder_one_week) { viewModel.updateSettings(UserSettingsUpdate(reminder_one_week = it)) }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.settings.extension_connected) {
                        Color(0xFFEAFBC5)
                    } else {
                        Color.White
                    }
                )
            ) {
                Column(Modifier.padding(17.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (state.settings.extension_connected) {
                                Icons.Default.Check
                            } else {
                                Icons.Default.Email
                            },
                            null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            when {
                                state.settings.extension_connected && isArabic ->
                                    "إضافة Outlook متصلة"
                                state.settings.extension_connected ->
                                    "Outlook extension connected"
                                isArabic -> "إضافة Outlook غير متصلة"
                                else -> "Outlook extension not connected"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        when {
                            state.settings.extension_connected && isArabic ->
                                "تم رصد اتصال حديث من إضافة Chrome."
                            state.settings.extension_connected ->
                                "A recent Chrome extension connection was detected."
                            isArabic ->
                                "سجّل الدخول إلى إضافة Chrome بالحساب نفسه لربط Outlook."
                            else ->
                                "Sign in to the Chrome extension with this account to connect Outlook."
                        },
                        color = MiyadTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
        }
        item {
            OutlinedButton(onClick = viewModel::logout, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(17.dp)) {
                Text(if (isArabic) "تسجيل الخروج" else "Log out", fontWeight = FontWeight.Bold)
            }
            Text(
                "Miyad ${BuildConfig.VERSION_NAME}",
                Modifier.fillMaxWidth().padding(12.dp),
                textAlign = TextAlign.Center,
                color = MiyadTextSecondary,
                fontSize = 11.sp
            )
            Text(if (isArabic) "لا نخزّن نصوص الرسائل أو بيانات دخول الجامعة." else "Raw emails and university credentials are never stored.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MiyadTextSecondary, fontSize = 11.sp)
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
fun NotificationsSettingsScreen(state: AppUiState, isArabic: Boolean) {
    ProfileSettingsScreen(state, isArabic)
}

@Composable
private fun SettingsToggle(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MiyadTextSecondary, fontSize = 12.sp)
            }
            Switch(checked, onChecked)
        }
    }
}

@Composable
private fun ReminderToggle(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked, onChecked)
    }
}

@Composable
private fun EventCard(event: EventDto, isArabic: Boolean, onClick: (() -> Unit)?) {
    Card(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(45.dp).clip(RoundedCornerShape(14.dp)).background(eventColor(event.event_type).copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                Icon(eventIcon(event.event_type), null, tint = eventColor(event.event_type))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${typeLabel(event.event_type, isArabic)} · ${formatDate(event.due_date, isArabic)}", color = MiyadTextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun EmptyState(isArabic: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(92.dp).clip(CircleShape).background(Color(0xFFEAFBC5)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.School, null, Modifier.size(43.dp), tint = MiyadDarkBackground)
        }
        Spacer(Modifier.height(14.dp))
        Text(if (isArabic) "تقويمك هادئ الآن" else "Your calendar is clear", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(if (isArabic) "افتح رسالة في Outlook أو استخدم الاستخلاص الذكي." else "Open an Outlook email or use smart extraction.", color = MiyadTextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ErrorState(error: String?, isArabic: Boolean, retry: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(error ?: if (isArabic) "تعذر تحميل البيانات" else "Could not load data", textAlign = TextAlign.Center)
        TextButton(onClick = retry) { Text(if (isArabic) "إعادة المحاولة" else "Retry") }
    }
}

@Composable
private fun SkeletonCard() {
    Card(Modifier.fillMaxWidth().height(76.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE9EDE6))) {}
}

private fun typeLabel(type: String, arabic: Boolean): String = when (type) {
    "exam" -> if (arabic) "اختبار" else "Exam"
    "deadline" -> if (arabic) "موعد تسليم" else "Deadline"
    "quiz" -> if (arabic) "اختبار قصير" else "Quiz"
    "lecture" -> if (arabic) "محاضرة" else "Lecture"
    else -> if (arabic) "أخرى" else "Other"
}

private fun eventColor(type: String) = when (type) {
    "exam" -> Color(0xFF6EA900)
    "deadline" -> Color(0xFFFFA000)
    "quiz" -> Color(0xFF8E6AC8)
    "lecture" -> Color(0xFF2388C9)
    else -> Color(0xFF657064)
}

private fun eventIcon(type: String): ImageVector = when (type) {
    "lecture" -> Icons.Default.School
    "deadline" -> Icons.Default.Schedule
    else -> Icons.Default.CalendarMonth
}

private fun formatDate(value: String, arabic: Boolean): String = runCatching {
    val date = OffsetDateTime.parse(value)
    val locale = if (arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
    date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale))
}.getOrDefault(value)
