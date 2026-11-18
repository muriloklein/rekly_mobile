package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Payment
import com.example.rekly.data.entity.Subscription
import com.example.rekly.data.local.DefaultCategories
import com.example.rekly.domain.model.PaymentStatus
import com.example.rekly.domain.model.SavingsSuggestion
import com.example.rekly.domain.model.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class SavingsSuggestionsTest {

    private val today = LocalDate.of(2026, 11, 18)

    private fun subscription(
        id: Long = 1,
        serviceName: String = "Netflix",
        categoryId: Long = DefaultCategories.STREAMING,
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
        startDate: LocalDate? = LocalDate.of(2026, 1, 10)
    ) = Subscription(
        id = id,
        userId = 1,
        categoryId = categoryId,
        serviceName = serviceName,
        amountCents = 3990,
        startDate = startDate,
        billingDay = 10,
        status = status,
        createdAt = LocalDate.of(2026, 1, 10)
    )

    private fun pending(subscriptionId: Long, dueDate: LocalDate, status: PaymentStatus = PaymentStatus.PENDING) =
        Payment(
            subscriptionId = subscriptionId,
            referenceMonth = YearMonth.from(dueDate),
            amountCents = 18000,
            dueDate = dueDate,
            status = status
        )

    // Overlap

    @Test
    fun two_active_streaming_services_are_an_overlap() {
        val suggestions = SavingsSuggestions.overlaps(
            listOf(subscription(1, "Netflix"), subscription(2, "Max"))
        )
        assertEquals(
            listOf(SavingsSuggestion.Overlap(DefaultCategories.STREAMING, listOf("Max", "Netflix"))),
            suggestions
        )
    }

    @Test
    fun one_service_per_category_is_not_an_overlap() {
        val suggestions = SavingsSuggestions.overlaps(
            listOf(subscription(1, "Netflix"), subscription(2, "Spotify", DefaultCategories.MUSIC))
        )
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun trial_and_canceled_do_not_count_as_overlap() {
        val suggestions = SavingsSuggestions.overlaps(
            listOf(
                subscription(1, "Netflix"),
                subscription(2, "Max", status = SubscriptionStatus.TRIAL),
                subscription(3, "Disney+", status = SubscriptionStatus.CANCELED)
            )
        )
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun bills_in_housing_are_not_an_overlap() {
        val suggestions = SavingsSuggestions.overlaps(
            listOf(
                subscription(1, "Aluguel", DefaultCategories.HOUSING),
                subscription(2, "Luz", DefaultCategories.HOUSING),
                subscription(3, "Água", DefaultCategories.HOUSING)
            )
        )
        assertTrue(suggestions.isEmpty())
    }

    // Long trial

    @Test
    fun trial_for_more_than_30_days_is_a_suggestion() {
        val trial = subscription(5, "Max", status = SubscriptionStatus.TRIAL, startDate = today.minusDays(31))
        assertEquals(
            listOf(SavingsSuggestion.LongTrial(5, "Max", 31)),
            SavingsSuggestions.longTrials(listOf(trial), today)
        )
    }

    @Test
    fun trial_for_exactly_30_days_is_not_yet_a_suggestion() {
        val trial = subscription(status = SubscriptionStatus.TRIAL, startDate = today.minusDays(30))
        assertTrue(SavingsSuggestions.longTrials(listOf(trial), today).isEmpty())
    }

    @Test
    fun trial_without_start_date_counts_from_creation() {
        // Created on 2026-01-10, long before today
        val trial = subscription(status = SubscriptionStatus.TRIAL, startDate = null)
        assertEquals(1, SavingsSuggestions.longTrials(listOf(trial), today).size)
    }

    // Stale pending

    @Test
    fun pending_for_more_than_7_days_is_a_suggestion() {
        val bill = subscription(3, "Luz", DefaultCategories.HOUSING)
        assertEquals(
            listOf(SavingsSuggestion.StalePending(3, "Luz", 8)),
            SavingsSuggestions.stalePendings(listOf(bill), listOf(pending(3, today.minusDays(8))), today)
        )
    }

    @Test
    fun pending_for_exactly_7_days_is_not_yet_a_suggestion() {
        val bill = subscription(3, "Luz", DefaultCategories.HOUSING)
        assertTrue(
            SavingsSuggestions.stalePendings(listOf(bill), listOf(pending(3, today.minusDays(7))), today).isEmpty()
        )
    }

    @Test
    fun paid_payment_is_never_a_suggestion() {
        val bill = subscription(3, "Luz", DefaultCategories.HOUSING)
        val paid = pending(3, today.minusDays(40), PaymentStatus.PAID)
        assertTrue(SavingsSuggestions.stalePendings(listOf(bill), listOf(paid), today).isEmpty())
    }

    // All together

    @Test
    fun build_returns_overlaps_then_trials_then_pendings() {
        val subscriptions = listOf(
            subscription(1, "Netflix"),
            subscription(2, "Max"),
            subscription(3, "Luz", DefaultCategories.HOUSING),
            subscription(4, "Deezer", DefaultCategories.MUSIC, SubscriptionStatus.TRIAL, today.minusDays(45))
        )
        val suggestions = SavingsSuggestions.build(subscriptions, listOf(pending(3, today.minusDays(10))), today)

        assertEquals(
            listOf(
                SavingsSuggestion.Overlap::class,
                SavingsSuggestion.LongTrial::class,
                SavingsSuggestion.StalePending::class
            ),
            suggestions.map { it::class }
        )
    }
}
