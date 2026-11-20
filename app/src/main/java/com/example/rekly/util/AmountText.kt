package com.example.rekly.util

import android.content.Context
import android.text.style.RelativeSizeSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.example.rekly.R

/** "R$ 55,90/mês" with the "/mês" suffix smaller than the amount. */
fun Context.monthlyAmountText(cents: Long, suffixSize: Float): CharSequence = buildSpannedString {
    append(MoneyFormatter.format(cents))
    inSpans(RelativeSizeSpan(suffixSize)) { append(getString(R.string.subscription_per_month)) }
}
