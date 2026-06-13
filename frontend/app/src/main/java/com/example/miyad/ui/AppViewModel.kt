package com.example.miyad.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miyad.data.EventDto
import com.example.miyad.data.MiyadRepository
import com.example.miyad.data.TokenStore
import com.example.miyad.data.UserDto
import com.example.miyad.data.UserSettingsDto
import com.example.miyad.data.UserSettingsUpdate
import com.example.miyad.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val initialized: Boolean = false,
    val language: String = "ar",
    val onboardingComplete: Boolean = false,
    val authenticated: Boolean = false,
    val user: UserDto? = null,
    val events: List<EventDto> = emptyList(),
    val selectedEvent: EventDto? = null,
    val extractionPreview: List<EventDto> = emptyList(),
    val settings: UserSettingsDto = UserSettingsDto(),
    val loading: Boolean = false,
    val extractionLoading: Boolean = false,
    val extractionSaved: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = TokenStore(application)
    private val repository = MiyadRepository(tokenStore)
    private val reminderScheduler = ReminderScheduler(application)
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        val hasToken = tokenStore.token != null
        _state.update {
            it.copy(
                initialized = true,
                language = tokenStore.language,
                onboardingComplete = tokenStore.onboardingComplete,
                authenticated = hasToken,
                user = tokenStore.user
            )
        }
        if (hasToken) refresh()
    }

    fun setLanguage(language: String) {
        tokenStore.language = language
        tokenStore.user = tokenStore.user?.copy(preferred_language = language)
        _state.update { it.copy(language = language) }
        if (_state.value.authenticated) {
            viewModelScope.launch {
                runCatching {
                    repository.updateSettings(
                        UserSettingsUpdate(preferred_language = language)
                    )
                }
            }
        }
    }

    fun completeOnboarding() {
        tokenStore.onboardingComplete = true
        _state.update { it.copy(onboardingComplete = true) }
    }

    fun login(email: String, password: String) {
        val validation = validateLogin(email, password)
        if (validation != null) {
            showAuthValidationError(validation)
            return
        }
        authenticate {
            repository.login(email, password)
        }
    }

    fun register(email: String, password: String, name: String, university: String) {
        val validation = validateRegistration(email, password, name, university)
        if (validation != null) {
            showAuthValidationError(validation)
            return
        }
        authenticate { repository.register(email, password, name, university) }
    }

    private fun authenticate(block: suspend () -> UserDto) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = runCatching { block() }
            val user = result.getOrElse { error ->
                _state.update {
                    it.copy(loading = false, error = repository.friendlyError(error))
                }
                return@launch
            }
            val selectedLanguage = tokenStore.language
            runCatching {
                repository.updateSettings(
                    UserSettingsUpdate(preferred_language = selectedLanguage)
                )
            }
            val localizedUser = user.copy(preferred_language = selectedLanguage)
            tokenStore.user = localizedUser
            _state.update {
                it.copy(
                    authenticated = true,
                    user = localizedUser,
                    language = selectedLanguage,
                    loading = false,
                    message = null
                )
            }
            refresh()
        }
    }

    fun refresh(type: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val events = repository.events(type)
                val settings = repository.settings()
                events to settings
            }.onSuccess { (events, settings) ->
                reminderScheduler.sync(events, settings, settings.preferred_language)
                _state.update {
                    it.copy(
                        events = events,
                        settings = settings,
                        language = settings.preferred_language,
                        loading = false
                    )
                }
                tokenStore.language = settings.preferred_language
            }.onFailure { error ->
                if (repository.friendlyError(error).contains("401")) logout()
                _state.update {
                    it.copy(loading = false, error = repository.friendlyError(error))
                }
            }
        }
    }

    fun selectEvent(event: EventDto?) {
        _state.update { it.copy(selectedEvent = event) }
    }

    fun deleteSelectedEvent() {
        val event = _state.value.selectedEvent ?: return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.deleteEvent(event.id) }
                .onSuccess {
                    val remainingEvents = _state.value.events.filterNot { it.id == event.id }
                    reminderScheduler.sync(
                        remainingEvents,
                        _state.value.settings,
                        _state.value.language
                    )
                    _state.update {
                        it.copy(
                            events = it.events.filterNot { item -> item.id == event.id },
                            selectedEvent = null,
                            loading = false,
                            message = if (it.language == "ar") "تم حذف الموعد" else "Event deleted"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, error = repository.friendlyError(error))
                    }
                }
        }
    }

    fun previewExtraction(text: String) = extract(text, save = false)
    fun saveExtraction(text: String) = extract(text, save = true)

    private fun extract(text: String, save: Boolean) {
        if (text.isBlank()) {
            _state.update {
                it.copy(error = if (it.language == "ar") "ألصق نص الرسالة أولاً" else "Paste email text first")
            }
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    extractionLoading = true,
                    extractionSaved = false,
                    error = null
                )
            }
            runCatching { repository.extract(text, save) }
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            extractionLoading = false,
                            extractionSaved = save && response.events.isNotEmpty(),
                            extractionPreview = response.events,
                            message = when {
                                response.events.isEmpty() && it.language == "ar" -> "لم يتم العثور على موعد"
                                response.events.isEmpty() -> "No academic event found"
                                save && it.language == "ar" -> "تم حفظ المواعيد بنجاح"
                                save -> "Events saved successfully"
                                else -> null
                            }
                        )
                    }
                    if (save) refresh()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            extractionLoading = false,
                            extractionSaved = false,
                            error = repository.friendlyError(error)
                        )
                    }
                }
        }
    }

    fun updateSettings(update: UserSettingsUpdate) {
        viewModelScope.launch {
            runCatching { repository.updateSettings(update) }
                .onSuccess { settings ->
                    reminderScheduler.sync(_state.value.events, settings, settings.preferred_language)
                    tokenStore.language = settings.preferred_language
                    _state.update {
                        it.copy(
                            settings = settings,
                            language = settings.preferred_language
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = repository.friendlyError(error)) }
                }
        }
    }

    fun logout() {
        reminderScheduler.cancelAll()
        repository.logout()
        _state.update {
            AppUiState(
                initialized = true,
                language = it.language,
                onboardingComplete = true
            )
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }

    private fun showAuthValidationError(error: AuthValidationError) {
        _state.update { state ->
            val arabic = state.language == "ar"
            val message = when (error) {
                AuthValidationError.EMAIL_REQUIRED ->
                    if (arabic) "أدخل البريد الإلكتروني" else "Enter your email"
                AuthValidationError.EMAIL_INVALID ->
                    if (arabic) "أدخل بريدًا إلكترونيًا صالحًا" else
                        "Enter a valid email address"
                AuthValidationError.PASSWORD_REQUIRED ->
                    if (arabic) "أدخل كلمة المرور" else "Enter your password"
                AuthValidationError.PASSWORD_TOO_SHORT ->
                    if (arabic) "يجب أن تتكون كلمة المرور من 8 أحرف على الأقل" else
                        "Password must be at least 8 characters"
                AuthValidationError.NAME_REQUIRED ->
                    if (arabic) "أدخل اسمك" else "Enter your name"
                AuthValidationError.UNIVERSITY_REQUIRED ->
                    if (arabic) "أدخل اسم الجامعة" else "Enter your university"
            }
            state.copy(error = message)
        }
    }

    fun updateSelectedReminder(reminder: String) {
        val event = _state.value.selectedEvent ?: return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.updateReminder(event.id, reminder) }
                .onSuccess { updated ->
                    val events = _state.value.events.map {
                        if (it.id == updated.id) updated else it
                    }
                    reminderScheduler.sync(events, _state.value.settings, _state.value.language)
                    _state.update {
                        it.copy(
                            events = events,
                            selectedEvent = updated,
                            loading = false,
                            message = if (it.language == "ar") {
                                "تم تحديث التذكير"
                            } else {
                                "Reminder updated"
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(loading = false, error = repository.friendlyError(error))
                    }
                }
        }
    }
}
