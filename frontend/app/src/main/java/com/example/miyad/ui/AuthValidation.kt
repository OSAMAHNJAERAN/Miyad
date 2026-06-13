package com.example.miyad.ui

enum class AuthValidationError {
    EMAIL_REQUIRED,
    EMAIL_INVALID,
    PASSWORD_REQUIRED,
    PASSWORD_TOO_SHORT,
    NAME_REQUIRED,
    UNIVERSITY_REQUIRED
}

fun validateLogin(email: String, password: String): AuthValidationError? = when {
    email.isBlank() -> AuthValidationError.EMAIL_REQUIRED
    !email.isPlausibleEmail() -> AuthValidationError.EMAIL_INVALID
    password.isBlank() -> AuthValidationError.PASSWORD_REQUIRED
    else -> null
}

fun validateRegistration(
    email: String,
    password: String,
    name: String,
    university: String
): AuthValidationError? = when {
    name.isBlank() -> AuthValidationError.NAME_REQUIRED
    university.isBlank() -> AuthValidationError.UNIVERSITY_REQUIRED
    email.isBlank() -> AuthValidationError.EMAIL_REQUIRED
    !email.isPlausibleEmail() -> AuthValidationError.EMAIL_INVALID
    password.isBlank() -> AuthValidationError.PASSWORD_REQUIRED
    password.length < 8 -> AuthValidationError.PASSWORD_TOO_SHORT
    else -> null
}

private fun String.isPlausibleEmail(): Boolean =
    matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
