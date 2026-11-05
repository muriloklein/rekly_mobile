package com.example.rekly.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/** Subscription together with its category (N:1 relation), used in the list. */
data class SubscriptionWithCategory(
    @Embedded val subscription: Subscription,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category
)
