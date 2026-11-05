package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Payment
import com.example.rekly.data.entity.Subscription
import com.example.rekly.domain.model.PaymentStatus
import com.example.rekly.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.YearMonth

/**
 * Presumed payment: when the app opens, creates the payments for the reference months
 * whose due date has already arrived and that do not exist yet.
 */
object PaymentGenerator {

    /**
     * Payments missing for [subscription] up to [today].
     *
     * Rules:
     * - Only ACTIVE and complete subscriptions (trial and canceled do not generate).
     * - Reference months of the period's cycle (annual generates 1 per year, not 12).
     * - Due date up to today, inclusive.
     * - Only from the month of [Subscription.activeSince] onward, no retroactive payments.
     * - Skips the reference months in [existingMonths].
     * - Fixed amount goes in as PAID with the subscription's amount, paid on the due date.
     * - Variable amount goes in as PENDING with the last amount paid as an estimate
     *   (or the subscription's amount, if there is no payment yet).
     */
    fun generate(
        subscription: Subscription,
        existingMonths: Set<YearMonth>,
        lastPaidAmountCents: Long?,
        today: LocalDate
    ): List<Payment> {
        if (subscription.status != SubscriptionStatus.ACTIVE) return emptyList()
        val amount = subscription.amountCents ?: return emptyList()
        val startDate = subscription.startDate ?: return emptyList()
        val billingDay = subscription.billingDay ?: return emptyList()
        val generationStart = YearMonth.from(subscription.activeSince ?: return emptyList())

        return ExpenseCalculator.dueDates(startDate, subscription.period, billingDay)
            .takeWhile { (_, dueDate) -> !dueDate.isAfter(today) }
            .filter { (referenceMonth, _) -> referenceMonth >= generationStart && referenceMonth !in existingMonths }
            .map { (referenceMonth, dueDate) ->
                if (subscription.variableAmount) {
                    Payment(
                        subscriptionId = subscription.id,
                        referenceMonth = referenceMonth,
                        amountCents = lastPaidAmountCents ?: amount,
                        dueDate = dueDate,
                        paidDate = null,
                        status = PaymentStatus.PENDING
                    )
                } else {
                    Payment(
                        subscriptionId = subscription.id,
                        referenceMonth = referenceMonth,
                        amountCents = amount,
                        dueDate = dueDate,
                        paidDate = dueDate,
                        status = PaymentStatus.PAID
                    )
                }
            }
            .toList()
    }

    /**
     * Adjusts [Subscription.activeSince] when saving: sets [today] when the subscription
     * becomes ACTIVE (creation, converted trial, reactivated canceled one) and keeps the
     * previous date otherwise. [previous] is null on creation.
     */
    fun adjustActivation(previous: Subscription?, updated: Subscription, today: LocalDate): Subscription {
        val becameActive = updated.status == SubscriptionStatus.ACTIVE &&
            (previous == null || previous.status != SubscriptionStatus.ACTIVE)
        val activeSince = when {
            becameActive -> today
            updated.status == SubscriptionStatus.ACTIVE -> previous?.activeSince ?: today
            else -> previous?.activeSince
        }
        return updated.copy(activeSince = activeSince)
    }
}
