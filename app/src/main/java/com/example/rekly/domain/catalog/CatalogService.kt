package com.example.rekly.domain.catalog

import com.example.rekly.domain.model.BillingPeriod

/**
 * A service or bill the user can pick instead of typing everything.
 * It has no price on purpose: prices change over time and vary by plan.
 *
 * [id] is a stable text key, so screens pass only the id between them.
 */
data class CatalogService(
    val id: String,
    val name: String,
    val categoryId: Long,
    val period: BillingPeriod = BillingPeriod.MONTHLY,
    val variableAmount: Boolean = false
)
