package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Payment
import com.example.rekly.data.entity.Subscription
import com.example.rekly.data.local.DefaultCategories
import com.example.rekly.domain.model.PaymentStatus
import com.example.rekly.domain.model.SavingsSuggestion
import com.example.rekly.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Savings tips for the summary. Pure functions, without database or Android. */
object SavingsSuggestions {

    const val TRIAL_DAYS_LIMIT = 30L
    const val PENDING_DAYS_LIMIT = 7L

    /**
     * Only categories whose services replace each other. Two bills in Housing (rent and
     * electricity) or in Telecom (internet and phone) are normal, not an overlap.
     */
    val overlapCategories: Set<Long> = setOf(DefaultCategories.STREAMING, DefaultCategories.MUSIC)

    fun build(
        subscriptions: List<Subscription>,
        payments: List<Payment>,
        today: LocalDate
    ): List<SavingsSuggestion> =
        overlaps(subscriptions) + longTrials(subscriptions, today) + stalePendings(subscriptions, payments, today)

    /** Two or more ACTIVE subscriptions in the same overlap category. */
    fun overlaps(subscriptions: List<Subscription>): List<SavingsSuggestion.Overlap> =
        subscriptions.filter { it.status == SubscriptionStatus.ACTIVE && it.categoryId in overlapCategories }
            .groupBy { it.categoryId }
            .filterValues { it.size >= 2 }
            .map { (categoryId, items) ->
                SavingsSuggestion.Overlap(categoryId, items.map { it.serviceName }.sortedBy { it.lowercase() })
            }

    /** TRIAL for more than [TRIAL_DAYS_LIMIT] days, counted from the start date (or creation, if missing). */
    fun longTrials(subscriptions: List<Subscription>, today: LocalDate): List<SavingsSuggestion.LongTrial> =
        subscriptions.filter { it.status == SubscriptionStatus.TRIAL }
            .mapNotNull { subscription ->
                val days = ChronoUnit.DAYS.between(subscription.startDate ?: subscription.createdAt, today)
                if (days > TRIAL_DAYS_LIMIT) {
                    SavingsSuggestion.LongTrial(subscription.id, subscription.serviceName, days)
                } else {
                    null
                }
            }

    /** PENDING for more than [PENDING_DAYS_LIMIT] days after the due date. */
    fun stalePendings(
        subscriptions: List<Subscription>,
        payments: List<Payment>,
        today: LocalDate
    ): List<SavingsSuggestion.StalePending> {
        val namesById = subscriptions.associate { it.id to it.serviceName }
        return payments.filter { it.status == PaymentStatus.PENDING }
            .mapNotNull { payment ->
                val days = ChronoUnit.DAYS.between(payment.dueDate, today)
                val serviceName = namesById[payment.subscriptionId]
                if (days > PENDING_DAYS_LIMIT && serviceName != null) {
                    SavingsSuggestion.StalePending(payment.subscriptionId, serviceName, days)
                } else {
                    null
                }
            }
    }
}
