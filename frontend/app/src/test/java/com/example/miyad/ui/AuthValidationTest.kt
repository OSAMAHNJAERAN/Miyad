package com.example.miyad.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {
    @Test
    fun loginRequiresBothFields() {
        assertEquals(AuthValidationError.EMAIL_REQUIRED, validateLogin("", "password"))
        assertEquals(AuthValidationError.EMAIL_INVALID, validateLogin("invalid", "password"))
        assertEquals(AuthValidationError.PASSWORD_REQUIRED, validateLogin("a@b.co", ""))
        assertNull(validateLogin("a@b.co", "password"))
    }

    @Test
    fun registrationRequiresProfileAndStrongPassword() {
        assertEquals(
            AuthValidationError.NAME_REQUIRED,
            validateRegistration("a@b.co", "password", "", "University")
        )
        assertEquals(
            AuthValidationError.PASSWORD_TOO_SHORT,
            validateRegistration("a@b.co", "short", "Student", "University")
        )
        assertEquals(
            AuthValidationError.EMAIL_INVALID,
            validateRegistration("invalid", "password", "Student", "University")
        )
        assertNull(validateRegistration("a@b.co", "password", "Student", "University"))
    }
}
