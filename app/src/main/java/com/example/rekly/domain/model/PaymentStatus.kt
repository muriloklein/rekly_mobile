package com.example.rekly.domain.model

/**
 * - [PAID]: fixed amount (generated automatically) or variable amount already confirmed.
 * - [PENDING]: variable amount waiting for the user to confirm the real value.
 */
enum class PaymentStatus {
    PAID,
    PENDING
}
