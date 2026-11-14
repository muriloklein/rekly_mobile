package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Subscription
import com.example.rekly.domain.model.SubscriptionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SubscriptionFilterTest {

    private fun subscription(
        serviceName: String = "Netflix",
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE
    ) = Subscription(
        userId = 1,
        categoryId = 1,
        serviceName = serviceName,
        status = status,
        createdAt = LocalDate.of(2026, 11, 14)
    )

    @Test
    fun empty_query_and_no_status_matches_everything() {
        assertTrue(SubscriptionFilter.matches(subscription(), query = "", status = null))
    }

    @Test
    fun query_matches_part_of_the_name_ignoring_case() {
        assertTrue(SubscriptionFilter.matches(subscription("Netflix"), query = "FLIX", status = null))
    }

    @Test
    fun query_ignores_spaces_around_it() {
        assertTrue(SubscriptionFilter.matches(subscription("Netflix"), query = "  net ", status = null))
    }

    @Test
    fun query_not_in_the_name_does_not_match() {
        assertFalse(SubscriptionFilter.matches(subscription("Netflix"), query = "spotify", status = null))
    }

    @Test
    fun status_filter_keeps_only_that_status() {
        assertTrue(
            SubscriptionFilter.matches(
                subscription(status = SubscriptionStatus.TRIAL),
                query = "",
                status = SubscriptionStatus.TRIAL
            )
        )
        assertFalse(
            SubscriptionFilter.matches(
                subscription(status = SubscriptionStatus.ACTIVE),
                query = "",
                status = SubscriptionStatus.TRIAL
            )
        )
    }

    @Test
    fun query_and_status_must_both_match() {
        val canceledNetflix = subscription("Netflix", SubscriptionStatus.CANCELED)
        assertFalse(SubscriptionFilter.matches(canceledNetflix, query = "net", status = SubscriptionStatus.ACTIVE))
        assertTrue(SubscriptionFilter.matches(canceledNetflix, query = "net", status = SubscriptionStatus.CANCELED))
    }
}
