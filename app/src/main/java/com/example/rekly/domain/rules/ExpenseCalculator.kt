package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Subscription
import com.example.rekly.domain.model.BillingPeriod
import com.example.rekly.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.YearMonth

/**
 * Expense calculations ported from the web version. Pure functions, all in cents,
 * rounding half up (0.5 cent becomes 1).
 */
object ExpenseCalculator {

    /** Monthly amount = amount ÷ months of the period. */
    fun monthlyCents(amountCents: Long, period: BillingPeriod): Long =
        divideRounding(amountCents, period.months.toLong())

    /** Real cost = monthly amount ÷ participants (minimum 1), calculated at once so it only rounds at the end. */
    fun realCostCents(amountCents: Long, period: BillingPeriod, participants: Int): Long =
        divideRounding(amountCents, period.months.toLong() * participants.coerceAtLeast(1))

    /** Due date in the reference month; day 31 in a short month falls on the last day of the month. */
    fun dueDateInMonth(referenceMonth: YearMonth, billingDay: Int): LocalDate =
        referenceMonth.atDay(billingDay.coerceIn(1, referenceMonth.lengthOfMonth()))

    /**
     * Infinite, increasing sequence of reference months with a due date: starts in the
     * month of the start date and moves [BillingPeriod.months] months at a time. A due
     * date before the start date is discarded.
     */
    fun dueDates(
        startDate: LocalDate,
        period: BillingPeriod,
        billingDay: Int
    ): Sequence<Pair<YearMonth, LocalDate>> =
        generateSequence(YearMonth.from(startDate)) { it.plusMonths(period.months.toLong()) }
            .map { referenceMonth -> referenceMonth to dueDateInMonth(referenceMonth, billingDay) }
            .filter { (_, dueDate) -> !dueDate.isBefore(startDate) }

    /** Next due date from [today], inclusive (due today counts as next). */
    fun nextDueDate(
        startDate: LocalDate,
        period: BillingPeriod,
        billingDay: Int,
        today: LocalDate
    ): LocalDate =
        dueDates(startDate, period, billingDay)
            .map { (_, dueDate) -> dueDate }
            .first { !it.isBefore(today) }

    // Versions that take the subscription; they return null if it is still incomplete.

    fun monthlyCents(subscription: Subscription): Long? =
        subscription.amountCents?.let { monthlyCents(it, subscription.period) }

    fun realCostCents(subscription: Subscription): Long? =
        subscription.amountCents?.let { realCostCents(it, subscription.period, subscription.participants) }

    /** Null if incomplete or canceled. */
    fun nextDueDate(subscription: Subscription, today: LocalDate): LocalDate? {
        val startDate = subscription.startDate ?: return null
        val billingDay = subscription.billingDay ?: return null
        if (subscription.status == SubscriptionStatus.CANCELED) return null
        return nextDueDate(startDate, subscription.period, billingDay, today)
    }

    /** Sum of the monthly amounts of ACTIVE and complete subscriptions (trial is left out). */
    fun monthlyTotalCents(subscriptions: List<Subscription>): Long =
        subscriptions.filter { it.status == SubscriptionStatus.ACTIVE }
            .sumOf { monthlyCents(it) ?: 0L }

    /** Sum of the real costs (after splitting) of ACTIVE and complete subscriptions. */
    fun realCostTotalCents(subscriptions: List<Subscription>): Long =
        subscriptions.filter { it.status == SubscriptionStatus.ACTIVE }
            .sumOf { realCostCents(it) ?: 0L }

    /** Integer division rounding half up (non-negative values). */
    private fun divideRounding(value: Long, divisor: Long): Long =
        (value * 2 + divisor) / (divisor * 2)
}
