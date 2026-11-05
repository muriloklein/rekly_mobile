package com.example.rekly.domain.model

/** Reasons why an operation was blocked by a business rule. */
enum class RuleError {
    /** Empty name. */
    NAME_REQUIRED,

    /** A category (default or the user's) with this name already exists. */
    DUPLICATE_NAME,

    /** Default categories cannot be renamed or deleted. */
    DEFAULT_CATEGORY,

    /** A category with subscriptions cannot be deleted. */
    CATEGORY_IN_USE,

    /** A subscription with payments cannot be deleted; the alternative is to cancel it. */
    SUBSCRIPTION_HAS_PAYMENTS,

    /** Amount must be greater than zero. */
    INVALID_AMOUNT,

    /** Record no longer exists in the database. */
    NOT_FOUND,

    NAME_TOO_SHORT,
    INVALID_EMAIL,
    EMAIL_ALREADY_REGISTERED,
    PASSWORD_REQUIRED,
    PASSWORD_TOO_SHORT,
    PASSWORDS_DO_NOT_MATCH,
    CATEGORY_REQUIRED,
    INVALID_BILLING_DAY,
    INVALID_PARTICIPANTS,
    START_DATE_REQUIRED,
    START_DATE_IN_FUTURE,

    /** Same error for unknown e-mail and wrong password, so it does not reveal which e-mails exist. */
    INVALID_CREDENTIALS
}
