package com.example.rekly.domain.catalog

import com.example.rekly.data.local.DefaultCategories
import com.example.rekly.domain.model.BillingPeriod
import com.example.rekly.domain.model.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CatalogSubscriptionsTest {

    private val today = LocalDate.of(2026, 11, 14)

    @Test
    fun service_becomes_subscription_with_catalog_data() {
        val subscription = ServiceCatalog.toSubscriptions(listOf("car_insurance"), userId = 7, today = today).single()

        assertEquals(7L, subscription.userId)
        assertEquals("Seguro do carro", subscription.serviceName)
        assertEquals(DefaultCategories.INSURANCE, subscription.categoryId)
        assertEquals(BillingPeriod.ANNUAL, subscription.period)
        assertEquals(SubscriptionStatus.ACTIVE, subscription.status)
        assertEquals(today, subscription.createdAt)
    }

    @Test
    fun created_subscription_is_incomplete() {
        val subscription = ServiceCatalog.toSubscriptions(listOf("netflix"), userId = 1, today = today).single()

        assertNull(subscription.amountCents)
        assertNull(subscription.startDate)
        assertNull(subscription.billingDay)
        assertFalse(subscription.isComplete())
    }

    @Test
    fun variable_bill_keeps_variable_amount() {
        val subscription = ServiceCatalog.toSubscriptions(listOf("electricity"), userId = 1, today = today).single()

        assertTrue(subscription.variableAmount)
    }

    @Test
    fun keeps_catalog_order_regardless_of_selection_order() {
        val names = ServiceCatalog.toSubscriptions(listOf("spotify", "netflix"), userId = 1, today = today)
            .map { it.serviceName }

        assertEquals(listOf("Netflix", "Spotify"), names)
    }

    @Test
    fun unknown_ids_are_ignored() {
        assertTrue(ServiceCatalog.toSubscriptions(listOf("does_not_exist"), userId = 1, today = today).isEmpty())
    }
}
