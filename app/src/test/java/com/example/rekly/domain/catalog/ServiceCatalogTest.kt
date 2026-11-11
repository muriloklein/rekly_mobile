package com.example.rekly.domain.catalog

import com.example.rekly.data.local.DefaultCategories
import com.example.rekly.domain.model.BillingPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceCatalogTest {

    private val services = ServiceCatalog.services

    @Test
    fun ids_are_unique() {
        assertEquals(services.size, services.map { it.id }.toSet().size)
    }

    @Test
    fun names_are_unique_ignoring_case() {
        assertEquals(services.size, services.map { it.name.lowercase() }.toSet().size)
    }

    @Test
    fun every_service_uses_a_default_category() {
        services.forEach { service ->
            assertTrue(service.name, service.categoryId in DefaultCategories.all.keys)
        }
    }

    @Test
    fun every_default_category_has_at_least_one_service() {
        assertEquals(DefaultCategories.all.keys, ServiceCatalog.groupedByCategory().keys)
    }

    @Test
    fun groups_follow_the_order_of_the_default_categories() {
        assertEquals(DefaultCategories.all.keys.toList(), ServiceCatalog.groupedByCategory().keys.toList())
    }

    @Test
    fun find_by_id_returns_the_service() {
        val netflix = ServiceCatalog.findById("netflix")
        assertEquals("Netflix", netflix?.name)
        assertEquals(DefaultCategories.STREAMING, netflix?.categoryId)
        assertEquals(BillingPeriod.MONTHLY, netflix?.period)
        assertEquals(false, netflix?.variableAmount)
    }

    @Test
    fun find_by_unknown_id_returns_null() {
        assertNull(ServiceCatalog.findById("unknown"))
    }

    @Test
    fun water_electricity_and_gas_have_variable_amount() {
        listOf("water", "electricity", "gas").forEach { id ->
            assertEquals(id, true, ServiceCatalog.findById(id)?.variableAmount)
        }
    }

    @Test
    fun streaming_services_have_fixed_amount() {
        services.filter { it.categoryId == DefaultCategories.STREAMING }.forEach { service ->
            assertFalse(service.name, service.variableAmount)
        }
    }

    @Test
    fun car_and_home_insurance_are_annual() {
        assertEquals(BillingPeriod.ANNUAL, ServiceCatalog.findById("car_insurance")?.period)
        assertEquals(BillingPeriod.ANNUAL, ServiceCatalog.findById("home_insurance")?.period)
    }
}
