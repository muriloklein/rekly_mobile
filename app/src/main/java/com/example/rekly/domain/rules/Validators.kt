package com.example.rekly.domain.rules

import com.example.rekly.domain.model.RuleError

object Validators {

    private const val MIN_NAME_LENGTH = 3
    private const val MIN_PASSWORD_LENGTH = 6
    private val emailFormat = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validateName(name: String): RuleError? = when {
        name.isBlank() -> RuleError.NAME_REQUIRED
        name.trim().length < MIN_NAME_LENGTH -> RuleError.NAME_TOO_SHORT
        else -> null
    }

    fun validateEmail(email: String): RuleError? =
        if (emailFormat.matches(email.trim())) null else RuleError.INVALID_EMAIL

    fun validatePasswordFilled(password: String): RuleError? =
        if (password.isEmpty()) RuleError.PASSWORD_REQUIRED else null

    fun validateNewPassword(password: String): RuleError? =
        if (password.length < MIN_PASSWORD_LENGTH) RuleError.PASSWORD_TOO_SHORT else null

    fun validateConfirmation(password: String, confirmation: String): RuleError? =
        if (password == confirmation) null else RuleError.PASSWORDS_DO_NOT_MATCH
}
