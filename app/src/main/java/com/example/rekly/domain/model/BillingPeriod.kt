package com.example.rekly.domain.model

/**
 * How often the subscription is charged. [months] is used to convert the charged
 * amount into a monthly amount and to know how many months apart the due dates are.
 */
enum class BillingPeriod(val months: Int) {
    MONTHLY(1),
    QUARTERLY(3),
    SEMIANNUAL(6),
    ANNUAL(12)
}
