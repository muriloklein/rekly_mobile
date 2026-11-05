package com.example.rekly.domain.model

/**
 * State of the subscription.
 *
 * - [ACTIVE]: counts in the monthly total and generates presumed payments.
 * - [TRIAL]: free period; does not generate payments and is shown apart from the total.
 * - [CANCELED]: keeps the history, but does not generate new payments.
 */
enum class SubscriptionStatus {
    ACTIVE,
    TRIAL,
    CANCELED
}
