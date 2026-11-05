package com.example.rekly.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Subscription with its category (N:1) and payment history (1:N), used in the details screen.
 * Room does not guarantee the order of [payments]; sort them by reference month in the screen.
 */
data class SubscriptionDetails(
    @Embedded val subscription: Subscription,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category,
    @Relation(parentColumn = "id", entityColumn = "subscriptionId")
    val payments: List<Payment>
)
