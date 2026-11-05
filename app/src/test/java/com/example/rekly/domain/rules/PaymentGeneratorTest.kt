package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Subscription
import com.example.rekly.domain.model.BillingPeriod
import com.example.rekly.domain.model.PaymentStatus
import com.example.rekly.domain.model.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class PaymentGeneratorTest {

    private val today = LocalDate.of(2026, 10, 21)

    private fun subscription(
        amountCents: Long? = 3990,
        variableAmount: Boolean = false,
        period: BillingPeriod = BillingPeriod.MONTHLY,
        startDate: LocalDate? = LocalDate.of(2026, 1, 10),
        billingDay: Int? = 10,
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
        activeSince: LocalDate? = LocalDate.of(2026, 10, 1)
    ) = Subscription(
        id = 7,
        userId = 1,
        categoryId = 1,
        serviceName = "Netflix",
        amountCents = amountCents,
        variableAmount = variableAmount,
        period = period,
        startDate = startDate,
        billingDay = billingDay,
        status = status,
        activeSince = activeSince,
        createdAt = LocalDate.of(2026, 10, 1)
    )

    private fun generate(
        subscription: Subscription,
        existing: Set<YearMonth> = emptySet(),
        lastPaidAmount: Long? = null,
        today: LocalDate = this.today
    ) = PaymentGenerator.generate(subscription, existing, lastPaidAmount, today)

    // Fixed and variable amount

    @Test
    fun fixed_amount_generates_paid_on_the_due_date() {
        val payments = generate(subscription())

        assertEquals(1, payments.size)
        with(payments.single()) {
            assertEquals(7L, subscriptionId)
            assertEquals(YearMonth.of(2026, 10), referenceMonth)
            assertEquals(3990L, amountCents)
            assertEquals(LocalDate.of(2026, 10, 10), dueDate)
            assertEquals(LocalDate.of(2026, 10, 10), paidDate)
            assertEquals(PaymentStatus.PAID, status)
        }
    }

    @Test
    fun variable_amount_generates_pending_with_last_paid_amount() {
        val payment = generate(subscription(amountCents = 15000, variableAmount = true), lastPaidAmount = 18734).single()

        assertEquals(PaymentStatus.PENDING, payment.status)
        assertEquals(18734L, payment.amountCents)
        assertNull(payment.paidDate)
    }

    @Test
    fun variable_amount_without_history_uses_the_subscription_amount() {
        val payment = generate(subscription(amountCents = 15000, variableAmount = true)).single()
        assertEquals(15000L, payment.amountCents)
    }

    // When to generate

    @Test
    fun does_not_generate_before_the_due_date() {
        assertTrue(generate(subscription(), today = LocalDate.of(2026, 10, 9)).isEmpty())
    }

    @Test
    fun generates_on_the_due_date_itself() {
        assertEquals(1, generate(subscription(), today = LocalDate.of(2026, 10, 10)).size)
    }

    @Test
    fun does_not_generate_retroactively_before_the_activation_month() {
        val old = subscription(startDate = LocalDate.of(2022, 5, 10), activeSince = LocalDate.of(2026, 10, 15))
        val months = generate(old, today = LocalDate.of(2026, 12, 15)).map { it.referenceMonth }

        assertEquals(
            listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 11), YearMonth.of(2026, 12)),
            months
        )
    }

    @Test
    fun does_not_duplicate_existing_reference_months() {
        val months = generate(
            subscription(),
            existing = setOf(YearMonth.of(2026, 10), YearMonth.of(2026, 11)),
            today = LocalDate.of(2026, 12, 15)
        ).map { it.referenceMonth }

        assertEquals(listOf(YearMonth.of(2026, 12)), months)
    }

    @Test
    fun annual_generates_one_payment_per_year() {
        val annual = subscription(
            amountCents = 12000,
            period = BillingPeriod.ANNUAL,
            startDate = LocalDate.of(2025, 3, 15),
            billingDay = 15,
            activeSince = LocalDate.of(2025, 3, 15)
        )
        val months = generate(annual).map { it.referenceMonth }

        assertEquals(listOf(YearMonth.of(2025, 3), YearMonth.of(2026, 3)), months)
    }

    // When not to generate

    @Test
    fun trial_and_canceled_do_not_generate() {
        assertTrue(generate(subscription(status = SubscriptionStatus.TRIAL)).isEmpty())
        assertTrue(generate(subscription(status = SubscriptionStatus.CANCELED)).isEmpty())
    }

    @Test
    fun incomplete_does_not_generate() {
        assertTrue(generate(subscription(amountCents = null)).isEmpty())
        assertTrue(generate(subscription(startDate = null)).isEmpty())
        assertTrue(generate(subscription(billingDay = null)).isEmpty())
    }

    // Activation

    @Test
    fun creating_active_sets_active_since_today() {
        val updated = PaymentGenerator.adjustActivation(null, subscription(activeSince = null), today)
        assertEquals(today, updated.activeSince)
    }

    @Test
    fun creating_in_trial_does_not_set_activation() {
        val updated = PaymentGenerator.adjustActivation(
            null,
            subscription(status = SubscriptionStatus.TRIAL, activeSince = null),
            today
        )
        assertNull(updated.activeSince)
    }

    @Test
    fun converting_trial_to_active_sets_today() {
        val previous = subscription(status = SubscriptionStatus.TRIAL, activeSince = null)
        val updated = PaymentGenerator.adjustActivation(previous, previous.copy(status = SubscriptionStatus.ACTIVE), today)
        assertEquals(today, updated.activeSince)
    }

    @Test
    fun reactivating_canceled_sets_today() {
        val previous = subscription(status = SubscriptionStatus.CANCELED, activeSince = LocalDate.of(2025, 1, 1))
        val updated = PaymentGenerator.adjustActivation(previous, previous.copy(status = SubscriptionStatus.ACTIVE), today)
        assertEquals(today, updated.activeSince)
    }

    @Test
    fun editing_active_keeps_activation_date() {
        val previous = subscription(activeSince = LocalDate.of(2026, 3, 1))
        val updated = PaymentGenerator.adjustActivation(previous, previous.copy(amountCents = 4590), today)
        assertEquals(LocalDate.of(2026, 3, 1), updated.activeSince)
    }

    @Test
    fun canceling_keeps_activation_date() {
        val previous = subscription(activeSince = LocalDate.of(2026, 3, 1))
        val updated = PaymentGenerator.adjustActivation(
            previous,
            previous.copy(status = SubscriptionStatus.CANCELED),
            today
        )
        assertEquals(LocalDate.of(2026, 3, 1), updated.activeSince)
    }
}
