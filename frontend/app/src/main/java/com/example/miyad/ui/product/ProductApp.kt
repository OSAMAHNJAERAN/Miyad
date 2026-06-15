package com.example.miyad.ui.product

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miyad.BuildConfig
import com.example.miyad.R
import com.example.miyad.data.EventCreateRequest
import com.example.miyad.data.EventDto
import com.example.miyad.data.UserSettingsUpdate
import com.example.miyad.theme.MiyadDarkBackground
import com.example.miyad.theme.MiyadLime
import com.example.miyad.theme.LocalMiyadGlassColors
import com.example.miyad.theme.ThemeMode
import com.example.miyad.theme.ThmanyahSans
import com.example.miyad.theme.ThmanyahSerifDisplay
import com.example.miyad.ui.AppUiState
import com.example.miyad.ui.AppViewModel
import com.example.miyad.ui.components.GlassBackground
import com.example.miyad.ui.components.GlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

enum class AppScreen {
    Splash, Onboarding, Language, Auth, Home, Calendar, Extract, Statistics,
    Settings, Details, AcademicProfile, PendingAlerts
}

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
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                    AppScreen.Statistics -> MainShell(
                        selected = AppScreen.Statistics,
                        state = state,
                        isArabic = isArabic,
                        onNavigate = { screen = it },
                        onRefresh = viewModel::refresh,
                        onEvent = {
                            viewModel.selectEvent(it)
                            detailsReturnScreen = AppScreen.Statistics
                            screen = AppScreen.Details
                        }
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
                        onEdit = { request, onUpdated ->
                            viewModel.updateSelectedEvent(request, onUpdated)
                        },
                        onDelete = {
                            viewModel.deleteSelectedEvent()
                            screen = detailsReturnScreen
                        }
                    )
                    AppScreen.AcademicProfile -> AcademicProfileScreen(
                        state = state,
                        isArabic = isArabic,
                        onBack = { screen = AppScreen.Settings }
                    )
                    AppScreen.PendingAlerts -> PendingAlertsScreen(
                        state = state,
                        isArabic = isArabic,
                        onBack = { screen = AppScreen.Home }
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
    val scale by animateFloatAsState(
        if (visible) 1f else .72f,
        spring(dampingRatio = .72f, stiffness = 240f),
        label = "logo"
    )
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible, enter = fadeIn(tween(650)) + scaleIn(tween(650))) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.miyad_logo),
                    contentDescription = "Miyad",
                    modifier = Modifier.size(132.dp).scale(scale),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "مِيعاد",
                    fontFamily = ThmanyahSerifDisplay,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "MIYAD",
                    letterSpacing = 4.sp,
                    fontFamily = ThmanyahSans,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        Modifier.fillMaxSize().statusBarsPadding()
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
                        .background(
                            Brush.linearGradient(
                                listOf(MiyadLime, MaterialTheme.colorScheme.primaryContainer)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.third, null, Modifier.size(72.dp), tint = MiyadDarkBackground)
                }
                Spacer(Modifier.height(34.dp))
                Text(item.first, fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 30.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    item.second,
                    fontFamily = ThmanyahSans,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 25.sp
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            pages.indices.forEach { index ->
                Box(
                    Modifier.padding(4.dp).height(8.dp)
                        .width(if (index == pagerState.currentPage) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
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
        Icon(
            Icons.Default.Language,
            null,
            Modifier.size(54.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(22.dp))
        Text("اختر لغتك", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 34.sp)
        Text(
            "Choose your language",
            fontFamily = ThmanyahSans,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 17.sp
        )
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
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(
                    if (highlighted) MiyadLime else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(mark, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontFamily = ThmanyahSans, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    subtitle,
                    fontFamily = ThmanyahSans,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
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
            .navigationBarsPadding().imePadding().padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(strong = true) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(MiyadLime), contentAlignment = Alignment.Center) {
            Text("م", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 34.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(title, fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 31.sp)
        Text(
            subtitle,
            fontFamily = ThmanyahSans,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(25.dp))
        content()
        }
    }
}

@Composable
private fun AuthField(value: String, onValue: (String) -> Unit, label: String, password: Boolean = false) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        visualTransformation = if (password && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        trailingIcon = if (password) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        }
                    )
                }
            }
        } else {
            null
        },
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
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background
        )
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = MiyadLime, strokeWidth = 2.dp)
        else Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun MainShell(
    selected: AppScreen,
    state: AppUiState,
    isArabic: Boolean,
    onNavigate: (AppScreen) -> Unit,
    onRefresh: () -> Unit,
    onEvent: (EventDto) -> Unit
) {
    val viewModel: AppViewModel = viewModel()
    var showAddEvent by remember { mutableStateOf(false) }
    val keyboardVisible = WindowInsets.isImeVisible
    val addEventSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetScope = rememberCoroutineScope()
    fun dismissAddEvent() {
        sheetScope.launch {
            runCatching { addEventSheetState.hide() }
            showAddEvent = false
        }
    }
    BackHandler(enabled = showAddEvent) { dismissAddEvent() }
    LaunchedEffect(selected) {
        if (showAddEvent) dismissAddEvent()
    }
    if (showAddEvent) {
        ModalBottomSheet(
            onDismissRequest = { dismissAddEvent() },
            sheetState = addEventSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            AddEventSheetV2(
                isArabic = isArabic,
                loading = state.loading,
                onDismiss = { dismissAddEvent() },
                onSave = { request ->
                    viewModel.createEvent(request) {
                        dismissAddEvent()
                        onNavigate(AppScreen.Calendar)
                    }
                }
            )
        }
    }
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (!keyboardVisible) {
                FloatingPillNavigation(
                    selected = selected,
                    isArabic = isArabic,
                    onNavigate = onNavigate,
                    onAdd = { showAddEvent = true }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                AppScreen.Home -> HomeDashboardScreen(
                    state,
                    isArabic,
                    onRefresh,
                    onEvent,
                    onNavigate = onNavigate,
                    onExtract = { onNavigate(AppScreen.Extract) },
                    onAdd = { showAddEvent = true }
                )
                AppScreen.Calendar -> CalendarScreen(state, isArabic, onRefresh, onEvent)
                AppScreen.Extract -> SmartExtractionScreen(state, isArabic)
                AppScreen.Statistics -> StatisticsScreen(state, isArabic)
                AppScreen.Settings -> ProfileSettingsScreen(state, isArabic, onNavigate)
                else -> Unit
            }
        }
    }
}

@Composable
private fun FloatingPillNavigation(
    selected: AppScreen,
    isArabic: Boolean,
    onNavigate: (AppScreen) -> Unit,
    onAdd: () -> Unit
) {
    val items = listOf(
        Triple(AppScreen.Home, Icons.Default.Home, if (isArabic) "الرئيسية" else "Home"),
        Triple(AppScreen.Calendar, Icons.Default.CalendarMonth, if (isArabic) "جدولي" else "Calendar"),
        Triple(AppScreen.Statistics, Icons.Default.BarChart, if (isArabic) "إحصائياتي" else "Statistics"),
        Triple(AppScreen.Settings, Icons.Default.AccountCircle, if (isArabic) "حسابي" else "Account")
    )
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(78.dp).shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(40.dp),
                ambientColor = Color.Black.copy(alpha = .18f),
                spotColor = Color.Black.copy(alpha = .18f)
            ),
            shape = RoundedCornerShape(40.dp),
            color = LocalMiyadGlassColors.current.strongSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                LocalMiyadGlassColors.current.border
            )
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavPillItem(items[0], selected, onNavigate, Modifier.weight(1f))
                NavPillItem(items[1], selected, onNavigate, Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
                NavPillItem(items[2], selected, onNavigate, Modifier.weight(1f))
                NavPillItem(items[3], selected, onNavigate, Modifier.weight(1f))
            }
        }
        Surface(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-13).dp)
                .size(76.dp).shadow(18.dp, CircleShape),
            shape = CircleShape,
            color = MiyadLime
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Add,
                    if (isArabic) "إضافة موعد" else "Add event",
                    Modifier.size(38.dp),
                    tint = MiyadDarkBackground
                )
            }
        }
    }
}

@Composable
private fun NavPillItem(
    item: Triple<AppScreen, ImageVector, String>,
    selected: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier
) {
    val active = selected == item.first
    val scale by animateFloatAsState(
        if (active) 1.08f else 1f,
        spring(dampingRatio = .58f, stiffness = 360f),
        label = "nav-${item.first}"
    )
    Column(
        modifier.clickable { onNavigate(item.first) }.padding(vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(34.dp).scale(scale).clip(CircleShape)
                .background(
                    if (active) MiyadLime.copy(alpha = .2f) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.second,
                item.third,
                Modifier.size(23.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            item.third,
            fontSize = 9.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun HomeDashboardScreen(
    state: AppUiState,
    isArabic: Boolean,
    onRefresh: () -> Unit,
    onEvent: (EventDto) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onExtract: () -> Unit = {},
    onAdd: () -> Unit = {},
) {
    val metrics = dashboardMetrics(state.events)
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (isArabic) "مرحباً ${state.user?.name ?: ""}" else "Hello ${state.user?.name ?: ""}", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 28.sp)
                    Text(
                        if (isArabic) "هذه نظرة على أسبوعك الأكاديمي" else "Here is your academic week",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, if (isArabic) "تحديث" else "Refresh") }
            }
        }
        if (state.alerts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.PendingAlerts) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isArabic) "لديك مواعيد غير مؤكدة (${state.alerts.size})" else "Unconfirmed events (${state.alerts.size})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                if (isArabic) "اضغط هنا لمراجعتها وتأكيدها بالـ AI." else "Tap to review and confirm them with AI.",
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = if (isArabic) "إضافة موعد" else "Add event",
                    subtitle = if (isArabic) "أدخل موعدًا بسرعة" else "Create one quickly",
                    icon = Icons.Default.Add,
                    onClick = onAdd,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = if (isArabic) "استخلاص ذكي" else "Smart extract",
                    subtitle = if (isArabic) "من رسالة جامعية" else "From an email",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onExtract,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Surface(
                onClick = onExtract,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(MiyadLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = MiyadDarkBackground)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (isArabic) "الاستخلاص الذكي" else "Smart extraction",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isArabic) "ألصق رسالة جامعية وحوّلها إلى مواعيد" else
                                "Turn a university email into calendar events",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LocalMiyadGlassColors.current.strongSurface
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(17.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (isArabic) "مسار الأسبوع" else "Week timeline",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isArabic) {
                                "اليوم ${LocalDate.now().dayOfWeek.value} من 7"
                            } else {
                                "Day ${LocalDate.now().dayOfWeek.value} of 7"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { metrics.weekProgress },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(7.dp)
                            .clip(CircleShape),
                        color = MiyadLime,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
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
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 108.dp),
        shape = RoundedCornerShape(22.dp),
        color = LocalMiyadGlassColors.current.strongSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            LocalMiyadGlassColors.current.border
        ),
        shadowElevation = 3.dp,
    ) {
        Column(
            Modifier.padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                    .background(MiyadLime.copy(alpha = .22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(20.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(17.dp)) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 25.sp)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CounterCard(label: String, count: Int, modifier: Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMiyadGlassColors.current.strongSurface)
    ) {
        Column(Modifier.padding(vertical = 14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
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
private fun AddEventSheet(
    isArabic: Boolean,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSave: (EventCreateRequest) -> Unit
) {
    val now = remember { LocalDateTime.now().withSecond(0).withNano(0) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(now.toLocalDate().toString()) }
    var startTime by remember { mutableStateOf(now.toLocalTime().toString()) }
    var endTime by remember { mutableStateOf(now.plusHours(1).toLocalTime().toString()) }
    var allDay by remember { mutableStateOf(false) }
    var repeat by remember { mutableStateOf("none") }
    var location by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf("one_day") }
    var color by remember { mutableStateOf("#BCDA4B") }
    var error by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    if (isArabic) "إضافة موعد" else "Add event",
                    fontFamily = ThmanyahSerifDisplay,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                )
                Text(
                    if (isArabic) "موعد مستقل بتوقيته وتذكيره" else
                        "An independent event with its own schedule",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إغلاق" else "Close")
            }
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(240)) + slideInVertically(tween(320)) { it / 8 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isArabic) "العنوان" else "Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(17.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isArabic) "الوصف" else "Description") },
                    minLines = 2,
                    shape = RoundedCornerShape(17.dp)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isArabic) "التاريخ YYYY-MM-DD" else "Date YYYY-MM-DD") },
                    singleLine = true,
                    shape = RoundedCornerShape(17.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        modifier = Modifier.weight(1f),
                        enabled = !allDay,
                        label = { Text(if (isArabic) "البداية" else "Start") },
                        singleLine = true,
                        shape = RoundedCornerShape(17.dp)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        modifier = Modifier.weight(1f),
                        enabled = !allDay,
                        label = { Text(if (isArabic) "النهاية" else "End") },
                        singleLine = true,
                        shape = RoundedCornerShape(17.dp)
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isArabic) "طوال اليوم" else "All day", Modifier.weight(1f))
                    Switch(checked = allDay, onCheckedChange = { allDay = it })
                }
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isArabic) "المكان أو الرابط" else "Location or link") },
                    singleLine = true,
                    shape = RoundedCornerShape(17.dp)
                )
                Text(if (isArabic) "التكرار" else "Repeat", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf("none", "daily", "weekly", "monthly", "custom").forEach { value ->
                        FilterChip(
                            selected = repeat == value,
                            onClick = { repeat = value },
                            label = { Text(repeatLabel(value, isArabic)) }
                        )
                    }
                }
                Text(if (isArabic) "التذكير" else "Reminder", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf("same_day", "one_day", "one_week", "none").forEach { value ->
                        FilterChip(
                            selected = reminder == value,
                            onClick = { reminder = value },
                            label = { Text(reminderLabel(value, isArabic)) }
                        )
                    }
                }
                Text(if (isArabic) "لون الموعد" else "Event color", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("#BCDA4B", "#2388C9", "#8E6AC8", "#FFA000", "#D94949")
                        .forEach { value ->
                            val selected = color == value
                            Box(
                                Modifier.size(34.dp).clip(CircleShape)
                                    .background(colorFromHex(value))
                                    .border(
                                        if (selected) 3.dp else 0.dp,
                                        MiyadDarkBackground,
                                        CircleShape
                                    )
                                    .clickable { color = value }
                            )
                        }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        val request = runCatching {
                            require(title.isNotBlank())
                            val localDate = LocalDate.parse(date)
                            val start = if (allDay) {
                                localDate.atStartOfDay()
                            } else {
                                LocalDateTime.of(localDate, LocalTime.parse(startTime))
                            }
                            val end = if (allDay) {
                                localDate.plusDays(1).atStartOfDay()
                            } else {
                                LocalDateTime.of(localDate, LocalTime.parse(endTime))
                            }
                            require(end.isAfter(start))
                            EventCreateRequest(
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                start_time = start.atZone(ZoneId.systemDefault())
                                    .toOffsetDateTime().toString(),
                                end_time = end.atZone(ZoneId.systemDefault())
                                    .toOffsetDateTime().toString(),
                                all_day = allDay,
                                repeat = repeat,
                                location = location.trim().ifBlank { null },
                                reminder = reminder,
                                event_color = color
                            )
                        }.getOrElse {
                            error = if (isArabic) {
                                "تحقق من العنوان والتاريخ وأن وقت النهاية بعد البداية"
                            } else {
                                "Check the title, date, and that end time follows start time"
                            }
                            return@Button
                        }
                        error = null
                        onSave(request)
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiyadLime,
                        contentColor = MiyadDarkBackground
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            Modifier.size(22.dp),
                            color = MiyadDarkBackground,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isArabic) "حفظ الموعد" else "Save event",
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun StatisticsScreen(state: AppUiState, isArabic: Boolean) {
    val metrics = dashboardMetrics(state.events)
    val typeCounts = listOf("exam", "deadline", "quiz", "lecture", "other")
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (isArabic) "إحصائياتي" else "Statistics",
                fontFamily = ThmanyahSerifDisplay,
                fontWeight = FontWeight.Black,
                fontSize = 29.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                if (isArabic) "ملخص هادئ لنشاطك الأكاديمي" else
                    "A clear view of your academic activity",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(
                    if (isArabic) "اليوم" else "Today",
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
            EventBreakdownChart(state.events, isArabic)
        }
        items(typeCounts) { type ->
            val count = state.events.count { it.event_type == type }
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LocalMiyadGlassColors.current.strongSurface
                )
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(17.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape)
                            .background(eventColor(type).copy(alpha = .16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(eventIcon(type), null, tint = eventColor(type))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(typeLabel(type, isArabic), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(count.toString(), fontWeight = FontWeight.Black, fontSize = 22.sp)
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
fun CalendarScreen(state: AppUiState, isArabic: Boolean, onRefresh: () -> Unit, onEvent: (EventDto) -> Unit) {
    var filter by remember { mutableStateOf<String?>(null) }
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val locale = if (isArabic) Locale.forLanguageTag("ar") else Locale.ENGLISH
    val monthDays = monthCalendarDays(displayedMonth)
    val visibleEvents = eventsForDate(state.events, selectedDate, filter)

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isArabic) "التقويم" else "Calendar", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 29.sp)
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, if (isArabic) "تحديث" else "Refresh")
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LocalMiyadGlassColors.current.strongSurface
                )
            ) {
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                val dayEvents = eventsForDate(state.events, day.date)
                                val hasEvent = dayEvents.isNotEmpty()
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
                                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                                else -> Color.Transparent
                                            }
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            day.date.dayOfMonth.toString(),
                                            color = when {
                                                isSelected -> Color.White
                                                !day.isInDisplayedMonth ->
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = .42f
                                                    )
                                                else -> Color.Unspecified
                                            },
                                            fontWeight = if (hasEvent || isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    Row(
                                        Modifier.padding(top = 3.dp).height(9.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        dayEvents.take(3).forEach { event ->
                                            Box(
                                                Modifier.size(4.dp).clip(CircleShape)
                                                    .background(colorFromHex(event.event_color))
                                            )
                                        }
                                        if (dayEvents.size > 3) {
                                            Text(
                                                "+${dayEvents.size - 3}",
                                                fontSize = 7.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 8.sp
                                            )
                                        }
                                    }
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EventDetailsScreen(
    event: EventDto?,
    isArabic: Boolean,
    loading: Boolean,
    onBack: () -> Unit,
    onReminder: (String) -> Unit,
    onEdit: (EventCreateRequest, () -> Unit) -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editScope = rememberCoroutineScope()
    fun dismissEdit() {
        editScope.launch {
            runCatching { editSheetState.hide() }
            showEdit = false
        }
    }
    BackHandler(enabled = showEdit) { dismissEdit() }
    if (showEdit && event != null) {
        ModalBottomSheet(
            onDismissRequest = { dismissEdit() },
            sheetState = editSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            AddEventSheetV2(
                isArabic = isArabic,
                loading = loading,
                initialEvent = event,
                onDismiss = { dismissEdit() },
                onSave = { request -> onEdit(request) { dismissEdit() } }
            )
        }
    }
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
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                if (isArabic) "رجوع" else "Back"
            )
        }
        if (event == null) {
            EmptyState(isArabic)
            return@Column
        }
        Spacer(Modifier.height(10.dp))
        Text(
            typeLabel(event.event_type, isArabic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(event.title, fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 39.sp)
        (event.course_name ?: event.course_code)?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
        Spacer(Modifier.height(22.dp))
        val startValue = event.start_time ?: event.due_date
        val scheduleValue = buildString {
            append(formatDate(startValue, isArabic))
            event.end_time?.let {
                append(" – ")
                append(formatDate(it, isArabic))
            }
        }
        DetailRow(
            Icons.Default.Schedule,
            if (isArabic) "التاريخ والوقت" else "Date and time",
            scheduleValue
        )
        DetailRow(Icons.Default.LocationOn, if (isArabic) "المكان" else "Location", event.location ?: if (isArabic) "غير محدد" else "Not specified")
        DetailRow(
            Icons.Default.Refresh,
            if (isArabic) "التكرار" else "Repeat",
            repeatLabel(event.repeat, isArabic)
        )
        DetailRow(
            Icons.Default.Email,
            if (isArabic) "المصدر" else "Source",
            event.source_email_subject
                ?: if (event.source_hash != null) "Outlook / Miyad AI"
                else if (isArabic) "إدخال يدوي" else "Manual"
        )
        event.source_email_sender?.let {
            DetailRow(
                Icons.Default.AccountCircle,
                if (isArabic) "المرسل" else "Sender",
                it
            )
        }
        event.evidence?.let {
            DetailRow(
                Icons.Default.AutoAwesome,
                if (isArabic) "الدليل من الرسالة" else "Email evidence",
                it
            )
        }
        (event.description ?: event.notes)?.takeIf { it.isNotBlank() }?.let {
            Card(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LocalMiyadGlassColors.current.strongSurface
                )
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(if (isArabic) "ملاحظات" else "Notes", fontWeight = FontWeight.Bold)
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 7.dp),
                        lineHeight = 23.sp
                    )
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Button(
            onClick = { showEdit = true },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(17.dp)
        ) {
            Icon(Icons.Default.Edit, null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (isArabic) "تعديل الموعد" else "Edit event",
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
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
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold, maxLines = 4)
        }
    }
}

@Composable
fun SmartExtractionScreen(state: AppUiState, isArabic: Boolean) {
    val viewModel: AppViewModel = viewModel()
    var text by remember { mutableStateOf("") }
    var reviewEvents by remember { mutableStateOf<List<EventDto>>(emptyList()) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.extractionPreview) {
        reviewEvents = state.extractionPreview
    }
    editingIndex?.let { index ->
        reviewEvents.getOrNull(index)?.let { event ->
            ExtractionEditDialog(
                event = event,
                isArabic = isArabic,
                onDismiss = { editingIndex = null },
                onSave = { updated ->
                    reviewEvents = reviewEvents.toMutableList().also { it[index] = updated }
                    editingIndex = null
                }
            )
        }
    }
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(if (isArabic) "الاستخلاص الذكي" else "Smart extraction", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Black, fontSize = 29.sp, modifier = Modifier.padding(top = 16.dp))
            Text(
                if (isArabic) "ألصق نص رسالة جامعية لمراجعة المواعيد قبل حفظها." else
                    "Paste a university email and review events before saving.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        if (reviewEvents.isNotEmpty()) {
            item {
                Text(
                    if (isArabic) "مراجعة العناصر المستخرجة" else "Review extracted events",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }
            itemsIndexed(reviewEvents) { index, event ->
                ExtractionReviewCard(
                    event = event,
                    isArabic = isArabic,
                    onEdit = { editingIndex = index }
                )
            }
            item {
                Button(
                    onClick = { viewModel.saveReviewedEvents(text, reviewEvents) },
                    enabled = !state.extractionLoading,
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
private fun ExtractionReviewCard(
    event: EventDto,
    isArabic: Boolean,
    onEdit: () -> Unit
) {
    val lowConfidence = event.confidence == "low"
    GlassCard(strong = true) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Black, fontSize = 17.sp)
                Text(
                    formatDate(event.due_date, isArabic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null)
                Spacer(Modifier.width(5.dp))
                Text(if (isArabic) "تعديل" else "Edit")
            }
        }
        event.course_name?.let {
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        event.location?.let {
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        event.evidence?.let {
            Text(
                if (isArabic) "الدليل: $it" else "Evidence: $it",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            color = if (lowConfidence) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
            } else {
                MiyadLime.copy(alpha = 0.18f)
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (lowConfidence) {
                    if (isArabic) "ثقة منخفضة: راجع الموعد قبل الحفظ" else
                        "Low confidence: review before saving"
                } else {
                    if (isArabic) "الثقة: ${event.confidence}" else
                        "Confidence: ${event.confidence}"
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                fontSize = 11.sp,
                color = if (lowConfidence) MaterialTheme.colorScheme.error else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ExtractionEditDialog(
    event: EventDto,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (EventDto) -> Unit
) {
    var title by remember(event) { mutableStateOf(event.title) }
    var dueDate by remember(event) { mutableStateOf(event.due_date) }
    var course by remember(event) {
        mutableStateOf(event.course_name ?: event.course_code.orEmpty())
    }
    var location by remember(event) { mutableStateOf(event.location.orEmpty()) }
    var notes by remember(event) { mutableStateOf(event.notes.orEmpty()) }
    var invalidDate by remember(event) { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isArabic) "تعديل الموعد المستخلص" else "Edit extracted event") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isArabic) "العنوان" else "Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = {
                        dueDate = it
                        invalidDate = false
                    },
                    label = { Text(if (isArabic) "التاريخ والوقت ISO" else "Date and time (ISO)") },
                    isError = invalidDate,
                    supportingText = if (invalidDate) {
                        { Text(if (isArabic) "أدخل تاريخًا مع المنطقة الزمنية" else "Use a timezone-aware ISO date") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text(if (isArabic) "المقرر" else "Course") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(if (isArabic) "المكان أو الرابط" else "Location or link") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isArabic) "ملاحظات" else "Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank() || runCatching { OffsetDateTime.parse(dueDate) }.isFailure) {
                    invalidDate = true
                    return@TextButton
                }
                onSave(
                    event.copy(
                        title = title.trim(),
                        due_date = dueDate,
                        course_name = course.ifBlank { null },
                        location = location.ifBlank { null },
                        notes = notes.ifBlank { null }
                    )
                )
            }) {
                Text(if (isArabic) "حفظ التعديل" else "Save changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
fun ProfileSettingsScreen(state: AppUiState, isArabic: Boolean, onNavigate: (AppScreen) -> Unit) {
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
            SettingsButton(
                Icons.Default.School,
                if (isArabic) "ملفي الأكاديمي" else "My Academic Profile",
                if (isArabic) "إدارة المواد والجدول الدراسي" else "Manage courses and class schedule",
                onClick = { onNavigate(AppScreen.AcademicProfile) }
            )
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
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsBrightness, null)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            if (isArabic) "مظهر التطبيق" else "Appearance",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isArabic) "اتبع النظام أو اختر وضعًا ثابتًا" else
                                "Follow the system or choose a fixed theme",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(
                            ThemeMode.System,
                            Icons.Default.SettingsBrightness,
                            if (isArabic) "النظام" else "System"
                        ),
                        Triple(
                            ThemeMode.Light,
                            Icons.Default.LightMode,
                            if (isArabic) "فاتح" else "Light"
                        ),
                        Triple(
                            ThemeMode.Dark,
                            Icons.Default.DarkMode,
                            if (isArabic) "داكن" else "Dark"
                        )
                    ).forEach { option ->
                        FilterChip(
                            selected = state.themeMode == option.first,
                            onClick = { viewModel.setThemeMode(option.first) },
                            leadingIcon = {
                                Icon(option.second, null, Modifier.size(18.dp))
                            },
                            label = { Text(option.third) }
                        )
                    }
                }
            }
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
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LocalMiyadGlassColors.current.strongSurface
                )
            ) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    ReminderToggle(if (isArabic) "في نفس اليوم" else "Same day", state.settings.reminder_same_day) { viewModel.updateSettings(UserSettingsUpdate(reminder_same_day = it)) }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ReminderToggle(if (isArabic) "قبل يوم" else "One day before", state.settings.reminder_one_day) { viewModel.updateSettings(UserSettingsUpdate(reminder_one_day = it)) }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ReminderToggle(if (isArabic) "قبل أسبوع" else "One week before", state.settings.reminder_one_week) { viewModel.updateSettings(UserSettingsUpdate(reminder_one_week = it)) }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.settings.extension_connected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        LocalMiyadGlassColors.current.strongSurface
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                if (isArabic) "لا نخزّن نصوص الرسائل أو بيانات دخول الجامعة." else
                    "Raw emails and university credentials are never stored.",
                Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
fun NotificationsSettingsScreen(state: AppUiState, isArabic: Boolean) {
    ProfileSettingsScreen(state, isArabic, onNavigate = {})
}

@Composable
private fun SettingsToggle(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalMiyadGlassColors.current.strongSurface
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Switch(checked, onChecked)
        }
    }
}

@Composable
private fun SettingsButton(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalMiyadGlassColors.current.strongSurface
        ),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
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
        colors = CardDefaults.cardColors(
            containerColor = LocalMiyadGlassColors.current.strongSurface
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(45.dp).clip(RoundedCornerShape(14.dp)).background(eventColor(event.event_type).copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                Icon(eventIcon(event.event_type), null, tint = eventColor(event.event_type))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "${typeLabel(event.event_type, isArabic)} · ${formatDate(event.due_date, isArabic)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyState(isArabic: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.miyad_logo),
            contentDescription = null,
            modifier = Modifier.size(92.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(14.dp))
        Text(if (isArabic) "تقويمك هادئ الآن" else "Your calendar is clear", fontFamily = ThmanyahSerifDisplay, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            if (isArabic) "افتح رسالة في Outlook أو استخدم الاستخلاص الذكي." else
                "Open an Outlook email or use smart extraction.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
    Card(
        Modifier.fillMaxWidth().height(76.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {}
}

private fun repeatLabel(value: String, arabic: Boolean): String = when (value) {
    "daily" -> if (arabic) "يومي" else "Daily"
    "weekly" -> if (arabic) "أسبوعي" else "Weekly"
    "monthly" -> if (arabic) "شهري" else "Monthly"
    "custom" -> if (arabic) "مخصص" else "Custom"
    else -> if (arabic) "بدون تكرار" else "Does not repeat"
}

private fun reminderLabel(value: String, arabic: Boolean): String = when (value) {
    "same_day" -> if (arabic) "نفس اليوم" else "Same day"
    "one_week" -> if (arabic) "قبل أسبوع" else "One week"
    "none" -> if (arabic) "بدون" else "None"
    else -> if (arabic) "قبل يوم" else "One day"
}

private fun colorFromHex(value: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(value))
}.getOrDefault(MiyadLime)

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

@Composable
fun AcademicProfileScreen(
    state: AppUiState,
    isArabic: Boolean,
    onBack: () -> Unit
) {
    val viewModel: AppViewModel = viewModel()
    var showAddCourse by remember { mutableStateOf(false) }
    var showAddSlot by remember { mutableStateOf(false) }

    var courseCode by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var teachingPlan by remember { mutableStateOf("") }

    var slotCourseCode by remember { mutableStateOf("") }
    var slotDay by remember { mutableStateOf("Sunday") }
    var slotStart by remember { mutableStateOf("08:30") }
    var slotEnd by remember { mutableStateOf("10:00") }
    var slotLocation by remember { mutableStateOf("") }

    if (showAddCourse) {
        AlertDialog(
            onDismissRequest = { showAddCourse = false },
            title = { Text(if (isArabic) "إضافة مقرر جديد" else "Add New Course") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text(if (isArabic) "رمز المقرر (مثال: CS101)" else "Course Code (e.g. CS101)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text(if (isArabic) "اسم المقرر" else "Course Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teachingPlan,
                        onValueChange = { teachingPlan = it },
                        label = { Text(if (isArabic) "الخطة الدراسية (نص/Syllabus)" else "Teaching Plan (text/Syllabus)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (courseCode.isNotBlank() && courseName.isNotBlank()) {
                            viewModel.addCourse(courseCode.trim(), courseName.trim(), teachingPlan.trim().ifBlank { null })
                            showAddCourse = false
                            courseCode = ""
                            courseName = ""
                            teachingPlan = ""
                        }
                    }
                ) {
                    Text(if (isArabic) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourse = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    if (showAddSlot) {
        val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val daysArabic = mapOf(
            "Sunday" to "الأحد", "Monday" to "الاثنين", "Tuesday" to "الثلاثاء",
            "Wednesday" to "الأربعاء", "Thursday" to "الخميس", "Friday" to "الجمعة", "Saturday" to "السبت"
        )
        AlertDialog(
            onDismissRequest = { showAddSlot = false },
            title = { Text(if (isArabic) "إضافة موعد محاضرة" else "Add Class Slot") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(if (isArabic) "اختر المقرر:" else "Select Course:", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.courses.forEach { course ->
                            FilterChip(
                                selected = slotCourseCode == course.course_code,
                                onClick = { slotCourseCode = course.course_code },
                                label = { Text(course.course_code) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = slotLocation,
                        onValueChange = { slotLocation = it },
                        label = { Text(if (isArabic) "القاعة / المكان" else "Room / Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(if (isArabic) "اليوم:" else "Day:", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        days.forEach { d ->
                            FilterChip(
                                selected = slotDay == d,
                                onClick = { slotDay = d },
                                label = { Text(if (isArabic) daysArabic[d] ?: d else d) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = slotStart,
                            onValueChange = { slotStart = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(if (isArabic) "وقت البدء (HH:MM)" else "Start Time") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = slotEnd,
                            onValueChange = { slotEnd = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(if (isArabic) "وقت الانتهاء (HH:MM)" else "End Time") },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (slotCourseCode.isNotBlank() && slotStart.isNotBlank() && slotEnd.isNotBlank()) {
                            viewModel.addScheduleSlot(slotCourseCode, slotDay, slotStart, slotEnd, slotLocation.trim().ifBlank { null })
                            showAddSlot = false
                            slotLocation = ""
                        }
                    }
                ) {
                    Text(if (isArabic) "إضافة" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSlot = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isArabic) "ملفي الأكاديمي" else "Academic Profile",
                    fontFamily = ThmanyahSerifDisplay,
                    fontWeight = FontWeight.Black,
                    fontSize = 29.sp
                )
            }
            Text(
                if (isArabic) "إدارة موادك الدراسية وجدولك الأسبوعي لتحسين مطابقة المواعيد." else
                    "Manage your academic courses and weekly schedule to improve event verification.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isArabic) "المقررات المسجلة (${state.courses.size})" else "Registered Courses (${state.courses.size})",
                    fontFamily = ThmanyahSerifDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                TextButton(onClick = { showAddCourse = true }) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (isArabic) "إضافة مادة" else "Add Course")
                }
            }
        }

        if (state.courses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LocalMiyadGlassColors.current.strongSurface)
                ) {
                    Box(Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (isArabic) "لم تقم بإضافة أي مواد بعد. أضف موادك لتمكين التحقق الذكي بالـ AI." else
                                "No courses added yet. Add courses to enable smart AI verification.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.courses) { course ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(course.course_code, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MiyadLime)
                            Text(course.course_name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            course.teaching_plan?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    it,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.deleteCourse(course.course_code) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD94949))
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isArabic) "جدول المحاضرات الأسبوعي" else "Weekly Class Schedule",
                    fontFamily = ThmanyahSerifDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                TextButton(
                    onClick = {
                        if (state.courses.isNotEmpty()) {
                            slotCourseCode = state.courses.first().course_code
                            showAddSlot = true
                        }
                    },
                    enabled = state.courses.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (isArabic) "إضافة حصة" else "Add Slot")
                }
            }
        }

        if (state.schedule.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LocalMiyadGlassColors.current.strongSurface)
                ) {
                    Box(Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (isArabic) "لا توجد محاضرات في جدولك الحالي." else "No classes in your schedule.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val daysArabic = mapOf(
                "Sunday" to "الأحد", "Monday" to "الاثنين", "Tuesday" to "الثلاثاء",
                "Wednesday" to "الأربعاء", "Thursday" to "الخميس", "Friday" to "الجمعة", "Saturday" to "السبت"
            )
            val sortedSchedule = state.schedule.sortedWith(compareBy({
                val daysOrder = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                daysOrder.indexOf(it.day_of_week)
            }, { it.start_time }))

            items(sortedSchedule) { slot ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val dayText = if (isArabic) daysArabic[slot.day_of_week] ?: slot.day_of_week else slot.day_of_week
                            Text("$dayText · ${slot.start_time.substring(0, 5)} - ${slot.end_time.substring(0, 5)}", fontWeight = FontWeight.Bold)
                            Text(slot.course_code, fontWeight = FontWeight.Black, color = MiyadLime)
                            slot.location?.let {
                                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { viewModel.deleteScheduleSlot(slot.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD94949))
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun PendingAlertsScreen(
    state: AppUiState,
    isArabic: Boolean,
    onBack: () -> Unit
) {
    val viewModel: AppViewModel = viewModel()

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isArabic) "مراجعة المواعيد" else "Review Events",
                    fontFamily = ThmanyahSerifDisplay,
                    fontWeight = FontWeight.Black,
                    fontSize = 29.sp
                )
            }
            Text(
                if (isArabic) "هذه أحداث تم استخلاصها من بريدك ولكنها تحتاج إلى تأكيدك قبل حفظها." else
                    "These events were extracted from your email but need your confirmation before saving.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        if (state.alerts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(54.dp), tint = MiyadLime)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        if (isArabic) "كل شيء ممتاز! لا توجد تنبيهات معلقة" else "All clear! No pending alerts",
                        fontFamily = ThmanyahSerifDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        } else {
            items(state.alerts) { alert ->
                val event = alert.event_data
                val isNotMatching = alert.alert_type == "not_matching"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNotMatching) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                        } else {
                            LocalMiyadGlassColors.current.strongSurface
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isNotMatching) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else LocalMiyadGlassColors.current.border
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text(
                                    "${typeLabel(event.event_type, isArabic)} · ${formatDate(event.due_date, isArabic)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                if (!event.course_code.isNullOrBlank()) {
                                    Text(
                                        if (isArabic) "المقرر: ${event.course_code}" else "Course: ${event.course_code}",
                                        fontWeight = FontWeight.Bold,
                                        color = MiyadLime,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isNotMatching) MaterialTheme.colorScheme.error else MiyadLime.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (isNotMatching) {
                                        if (isArabic) "غير متطابق" else "Not Matching"
                                    } else {
                                        if (isArabic) "يحتاج مراجعة" else "Needs Review"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNotMatching) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        alert.ai_reason?.let { reason ->
                            Spacer(Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp), tint = MiyadLime)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        reason,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.resolveAlert(alert.id, "reject") },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD94949))
                            ) {
                                Text(if (isArabic) "تجاهل" else "Dismiss", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.resolveAlert(alert.id, "confirm") },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MiyadLime,
                                    contentColor = MiyadDarkBackground
                                )
                            ) {
                                Text(if (isArabic) "تأكيد وإضافة" else "Confirm & Add", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
