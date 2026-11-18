package com.example.rekly.domain.model

/**
 * A savings tip shown on the summary. It is calculated every time the screen opens and is
 * never stored, so it disappears by itself once the situation is resolved.
 */
sealed interface SavingsSuggestion {

    /** Two or more active services that replace each other, such as two streaming services. */
    data class Overlap(val categoryId: Long, val serviceNames: List<String>) : SavingsSuggestion

    /** A free trial for a long time: it may start charging soon. */
    data class LongTrial(val subscriptionId: Long, val serviceName: String, val days: Long) : SavingsSuggestion

    /** A variable bill waiting for the real amount for a while. */
    data class StalePending(val subscriptionId: Long, val serviceName: String, val days: Long) : SavingsSuggestion
}
