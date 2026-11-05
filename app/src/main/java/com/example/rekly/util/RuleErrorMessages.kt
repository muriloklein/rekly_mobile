package com.example.rekly.util

import androidx.annotation.StringRes
import com.example.rekly.R
import com.example.rekly.domain.model.RuleError

@StringRes
fun RuleError.messageRes(): Int = when (this) {
    RuleError.NAME_REQUIRED -> R.string.error_name_required
    RuleError.DUPLICATE_NAME -> R.string.error_duplicate_name
    RuleError.DEFAULT_CATEGORY -> R.string.error_default_category
    RuleError.CATEGORY_IN_USE -> R.string.error_category_in_use
    RuleError.SUBSCRIPTION_HAS_PAYMENTS -> R.string.error_subscription_has_payments
    RuleError.INVALID_AMOUNT -> R.string.error_invalid_amount
    RuleError.NOT_FOUND -> R.string.error_not_found
    RuleError.NAME_TOO_SHORT -> R.string.error_name_too_short
    RuleError.INVALID_EMAIL -> R.string.error_invalid_email
    RuleError.EMAIL_ALREADY_REGISTERED -> R.string.error_email_already_registered
    RuleError.PASSWORD_REQUIRED -> R.string.error_password_required
    RuleError.PASSWORD_TOO_SHORT -> R.string.error_password_too_short
    RuleError.PASSWORDS_DO_NOT_MATCH -> R.string.error_passwords_do_not_match
    RuleError.CATEGORY_REQUIRED -> R.string.error_category_required
    RuleError.INVALID_BILLING_DAY -> R.string.error_invalid_billing_day
    RuleError.INVALID_PARTICIPANTS -> R.string.error_invalid_participants
    RuleError.START_DATE_REQUIRED -> R.string.error_start_date_required
    RuleError.START_DATE_IN_FUTURE -> R.string.error_start_date_in_future
    RuleError.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
}
