package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Subscription
import com.example.rekly.domain.model.BillingPeriod
import com.example.rekly.domain.model.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ExpenseCalculatorTest {

    private val today = LocalDate.of(2026, 10, 21)

    private fun subscription(
        amountCents: Long? = 3990,
        period: BillingPeriod = BillingPeriod.MONTHLY,
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
        participants: Int = 1,
        startDate: LocalDate? = LocalDate.of(2026, 1, 10),
        billingDay: Int? = 10,
        categoryId: Long = 1,
        serviceName: String = "Netflix"
    ) = Subscription(
        userId = 1,
        categoryId = categoryId,
        serviceName = serviceName,
        amountCents = amountCents,
        period = period,
        startDate = startDate,
        billingDay = billingDay,
        status = status,
        participants = participants,
        createdAt = today
    )

    // Monthly amount

    @Test
    fun monthly_amount_of_monthly_period_is_the_amount_itself() {
        assertEquals(3990L, ExpenseCalculator.monthlyCents(3990, BillingPeriod.MONTHLY))
    }

    @Test
    fun monthly_amount_divides_by_the_months_of_the_period() {
        assertEquals(1330L, ExpenseCalculator.monthlyCents(3990, BillingPeriod.QUARTERLY))
        assertEquals(1000L, ExpenseCalculator.monthlyCents(6000, BillingPeriod.SEMIANNUAL))
        assertEquals(1000L, ExpenseCalculator.monthlyCents(12000, BillingPeriod.ANNUAL))
    }

    @Test
    fun monthly_amount_rounds_half_cent_up() {
        assertEquals(833L, ExpenseCalculator.monthlyCents(9999, BillingPeriod.ANNUAL)) // 833.25
        assertEquals(1L, ExpenseCalculator.monthlyCents(3, BillingPeriod.SEMIANNUAL)) // 0.5
        assertEquals(0L, ExpenseCalculator.monthlyCents(5, BillingPeriod.ANNUAL)) // 0.4166
        assertEquals(1L, ExpenseCalculator.monthlyCents(7, BillingPeriod.ANNUAL)) // 0.5833
    }

    // Real cost

    @Test
    fun real_cost_divides_by_participants() {
        assertEquals(1995L, ExpenseCalculator.realCostCents(3990, BillingPeriod.MONTHLY, 2))
        assertEquals(1330L, ExpenseCalculator.realCostCents(3990, BillingPeriod.MONTHLY, 3))
    }

    @Test
    fun real_cost_combines_period_and_participants() {
        // 120.00 per year split among 4 = 2.50 per month
        assertEquals(250L, ExpenseCalculator.realCostCents(12000, BillingPeriod.ANNUAL, 4))
    }

    @Test
    fun real_cost_considers_at_least_one_participant() {
        assertEquals(3990L, ExpenseCalculator.realCostCents(3990, BillingPeriod.MONTHLY, 0))
        assertEquals(3990L, ExpenseCalculator.realCostCents(3990, BillingPeriod.MONTHLY, -2))
    }

    // Due date

    @Test
    fun day_31_in_short_month_falls_on_the_last_day() {
        assertEquals(LocalDate.of(2026, 2, 28), ExpenseCalculator.dueDateInMonth(YearMonth.of(2026, 2), 31))
        assertEquals(LocalDate.of(2028, 2, 29), ExpenseCalculator.dueDateInMonth(YearMonth.of(2028, 2), 31))
        assertEquals(LocalDate.of(2026, 4, 30), ExpenseCalculator.dueDateInMonth(YearMonth.of(2026, 4), 31))
        assertEquals(LocalDate.of(2026, 3, 31), ExpenseCalculator.dueDateInMonth(YearMonth.of(2026, 3), 31))
    }

    @Test
    fun next_monthly_due_date_already_passed_in_the_month_goes_to_the_next() {
        val next = ExpenseCalculator.nextDueDate(LocalDate.of(2026, 1, 10), BillingPeriod.MONTHLY, 10, today)
        assertEquals(LocalDate.of(2026, 11, 10), next)
    }

    @Test
    fun next_due_date_includes_today() {
        val next = ExpenseCalculator.nextDueDate(LocalDate.of(2026, 1, 21), BillingPeriod.MONTHLY, 21, today)
        assertEquals(today, next)
    }

    @Test
    fun next_annual_due_date_follows_the_start_month() {
        val next = ExpenseCalculator.nextDueDate(LocalDate.of(2025, 3, 15), BillingPeriod.ANNUAL, 15, today)
        assertEquals(LocalDate.of(2027, 3, 15), next)
    }

    @Test
    fun next_quarterly_due_date_with_day_31() {
        // Cycle: 01/31, 04/30, 07/31, 10/31
        val next = ExpenseCalculator.nextDueDate(LocalDate.of(2026, 1, 31), BillingPeriod.QUARTERLY, 31, today)
        assertEquals(LocalDate.of(2026, 10, 31), next)
    }

    @Test
    fun due_date_before_the_start_date_is_ignored() {
        // Started on the 20th with billing on the 5th: the first due date is in the next month
        val first = ExpenseCalculator.dueDates(LocalDate.of(2026, 10, 20), BillingPeriod.MONTHLY, 5).first()
        assertEquals(YearMonth.of(2026, 11) to LocalDate.of(2026, 11, 5), first)
    }

    // Versions with the subscription

    @Test
    fun incomplete_subscription_has_no_monthly_amount_nor_due_date() {
        val incomplete = subscription(amountCents = null, startDate = null, billingDay = null)
        assertNull(ExpenseCalculator.monthlyCents(incomplete))
        assertNull(ExpenseCalculator.realCostCents(incomplete))
        assertNull(ExpenseCalculator.nextDueDate(incomplete, today))
    }

    @Test
    fun canceled_subscription_has_no_next_due_date() {
        assertNull(ExpenseCalculator.nextDueDate(subscription(status = SubscriptionStatus.CANCELED), today))
    }

    @Test
    fun totals_consider_only_active_and_complete() {
        val subscriptions = listOf(
            subscription(amountCents = 3990, participants = 2), // 39.90 / cost 19.95
            subscription(amountCents = 12000, period = BillingPeriod.ANNUAL), // 10.00 / cost 10.00
            subscription(amountCents = 2190, status = SubscriptionStatus.TRIAL),
            subscription(amountCents = 5000, status = SubscriptionStatus.CANCELED),
            subscription(amountCents = null)
        )
        assertEquals(4990L, ExpenseCalculator.monthlyTotalCents(subscriptions))
        assertEquals(2995L, ExpenseCalculator.realCostTotalCents(subscriptions))
    }

    // Summary

    @Test
    fun trial_total_counts_only_trial_subscriptions() {
        val subscriptions = listOf(
            subscription(amountCents = 2190, status = SubscriptionStatus.TRIAL),
            subscription(amountCents = 12000, period = BillingPeriod.ANNUAL, status = SubscriptionStatus.TRIAL), // 10,00
            subscription(amountCents = 3990),
            subscription(amountCents = null, status = SubscriptionStatus.TRIAL)
        )
        assertEquals(3190L, ExpenseCalculator.trialMonthlyTotalCents(subscriptions))
    }

    @Test
    fun spending_by_category_sums_monthly_amounts_largest_first() {
        val subscriptions = listOf(
            subscription(amountCents = 3990, categoryId = 1),
            subscription(amountCents = 12000, period = BillingPeriod.ANNUAL, categoryId = 1), // 10,00
            subscription(amountCents = 150000, categoryId = 3),
            subscription(amountCents = 2190, categoryId = 2, status = SubscriptionStatus.TRIAL),
            subscription(amountCents = null, categoryId = 4)
        )
        assertEquals(listOf(3L to 150000L, 1L to 4990L), ExpenseCalculator.monthlyByCategory(subscriptions))
    }

    @Test
    fun upcoming_includes_due_dates_from_today_up_to_seven_days() {
        val subscriptions = listOf(
            subscription(billingDay = 29, serviceName = "Day 29"), // 8 days ahead: out
            subscription(billingDay = 28, serviceName = "Day 28"), // exactly 7 days ahead: in
            subscription(billingDay = 21, serviceName = "Today"),
            subscription(billingDay = 10, serviceName = "Day 10") // already passed: next is in November
        )

        val upcoming = ExpenseCalculator.upcomingDueDates(subscriptions, today, 7)

        assertEquals(listOf("Today", "Day 28"), upcoming.map { (subscription, _) -> subscription.serviceName })
        assertEquals(listOf(today, LocalDate.of(2026, 10, 28)), upcoming.map { (_, dueDate) -> dueDate })
    }

    @Test
    fun upcoming_ignores_trial_canceled_and_incomplete() {
        val subscriptions = listOf(
            subscription(billingDay = 22, status = SubscriptionStatus.TRIAL),
            subscription(billingDay = 22, status = SubscriptionStatus.CANCELED),
            subscription(billingDay = 22, amountCents = null)
        )
        assertEquals(emptyList<Pair<Subscription, LocalDate>>(), ExpenseCalculator.upcomingDueDates(subscriptions, today, 7))
    }
}
