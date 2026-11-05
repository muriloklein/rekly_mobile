package com.example.rekly.util

import androidx.annotation.StringRes
import com.example.rekly.R
import com.example.rekly.domain.model.BillingPeriod
import com.example.rekly.domain.model.SubscriptionStatus

@StringRes
fun BillingPeriod.labelRes(): Int = when (this) {
    BillingPeriod.MONTHLY -> R.string.period_monthly
    BillingPeriod.QUARTERLY -> R.string.period_quarterly
    BillingPeriod.SEMIANNUAL -> R.string.period_semiannual
    BillingPeriod.ANNUAL -> R.string.period_annual
}

@StringRes
fun SubscriptionStatus.labelRes(): Int = when (this) {
    SubscriptionStatus.ACTIVE -> R.string.status_active
    SubscriptionStatus.TRIAL -> R.string.status_trial
    SubscriptionStatus.CANCELED -> R.string.status_canceled
}
