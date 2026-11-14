package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Subscription
import com.example.rekly.domain.model.SubscriptionStatus

object SubscriptionFilter {

    fun matches(subscription: Subscription, query: String, status: SubscriptionStatus?): Boolean {
        val matchesName = subscription.serviceName.contains(query.trim(), ignoreCase = true)
        val matchesStatus = status == null || subscription.status == status
        return matchesName && matchesStatus
    }
}
