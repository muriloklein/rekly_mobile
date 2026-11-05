package com.example.rekly.domain.rules

import com.example.rekly.domain.model.RuleError
import java.time.LocalDate

object Validators {

    private const val MIN_NAME_LENGTH = 3
    private const val MIN_PASSWORD_LENGTH = 6
    private val billingDays = 1..31
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

    fun validateServiceName(name: String): RuleError? =
        if (name.isBlank()) RuleError.NAME_REQUIRED else null

    fun validateCategory(categoryId: Long?): RuleError? =
        if (categoryId == null) RuleError.CATEGORY_REQUIRED else null

    fun validateAmount(amountCents: Long?): RuleError? =
        if (amountCents == null || amountCents <= 0) RuleError.INVALID_AMOUNT else null

    fun validateBillingDay(billingDay: Int?): RuleError? =
        if (billingDay == null || billingDay !in billingDays) RuleError.INVALID_BILLING_DAY else null

    fun validateParticipants(participants: Int?): RuleError? =
        if (participants == null || participants < 1) RuleError.INVALID_PARTICIPANTS else null

    fun validateStartDate(startDate: LocalDate?, today: LocalDate): RuleError? = when {
        startDate == null -> RuleError.START_DATE_REQUIRED
        startDate.isAfter(today) -> RuleError.START_DATE_IN_FUTURE
        else -> null
    }
}
