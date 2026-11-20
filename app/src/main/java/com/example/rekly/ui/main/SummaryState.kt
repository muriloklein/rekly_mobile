package com.example.rekly.ui.main

import com.example.rekly.data.entity.Payment
import com.example.rekly.data.entity.SubscriptionWithCategory
import com.example.rekly.domain.model.SavingsSuggestion
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.domain.rules.ExpenseCalculator
import com.example.rekly.domain.rules.SavingsSuggestions
import java.time.LocalDate
import java.time.YearMonth

/** Everything the summary shows, already calculated. Amounts in cents; formatting is up to the screen. */
data class SummaryState(
    val hasSubscriptions: Boolean,
    val monthlyTotalCents: Long,
    val realCostTotalCents: Long,
    val trialCount: Int,
    val trialMonthlyCents: Long,
    val incompleteCount: Int,
    val upcoming: List<UpcomingDueDate>,
    val pending: List<PendingPayment>,
    val spendingByCategory: List<CategorySpending>,
    val suggestions: List<SavingsSuggestion>,
    val categoryNames: Map<Long, String>
) {

    data class UpcomingDueDate(
        val subscriptionId: Long,
        val serviceName: String,
        val dueDate: LocalDate,
        val amountCents: Long,
        val variableAmount: Boolean
    )

    data class PendingPayment(
        val paymentId: Long,
        val subscriptionId: Long,
        val serviceName: String,
        val referenceMonth: YearMonth,
        val estimateCents: Long
    )

    /** [percent] is the share of the category in the monthly total, from 0 to 100. */
    data class CategorySpending(val categoryName: String, val monthlyCents: Long, val percent: Int)

    companion object {
        const val UPCOMING_DAYS = 7L

        /** Builds the summary only with the rules in domain/rules, so the numbers match the rest of the app. */
        fun from(items: List<SubscriptionWithCategory>, pendingPayments: List<Payment>, today: LocalDate): SummaryState {
            val subscriptions = items.map { it.subscription }
            val categoryNames = items.associate { it.category.id to it.category.name }
            val serviceNames = subscriptions.associate { it.id to it.serviceName }
            val byCategory = ExpenseCalculator.monthlyByCategory(subscriptions)
            val categoryTotal = byCategory.sumOf { (_, cents) -> cents }

            return SummaryState(
                hasSubscriptions = subscriptions.isNotEmpty(),
                monthlyTotalCents = ExpenseCalculator.monthlyTotalCents(subscriptions),
                realCostTotalCents = ExpenseCalculator.realCostTotalCents(subscriptions),
                trialCount = subscriptions.count { it.status == SubscriptionStatus.TRIAL },
                trialMonthlyCents = ExpenseCalculator.trialMonthlyTotalCents(subscriptions),
                incompleteCount = subscriptions.count { !it.isComplete() && it.status != SubscriptionStatus.CANCELED },
                upcoming = ExpenseCalculator.upcomingDueDates(subscriptions, today, UPCOMING_DAYS)
                    .map { (subscription, dueDate) ->
                        UpcomingDueDate(
                            subscription.id,
                            subscription.serviceName,
                            dueDate,
                            subscription.amountCents ?: 0L,
                            subscription.variableAmount
                        )
                    },
                pending = pendingPayments.map { payment ->
                    PendingPayment(
                        payment.id,
                        payment.subscriptionId,
                        serviceNames[payment.subscriptionId].orEmpty(),
                        payment.referenceMonth,
                        payment.amountCents
                    )
                },
                spendingByCategory = byCategory.map { (categoryId, cents) ->
                    CategorySpending(categoryNames[categoryId].orEmpty(), cents, percentOf(cents, categoryTotal))
                },
                suggestions = SavingsSuggestions.build(subscriptions, pendingPayments, today),
                categoryNames = categoryNames
            )
        }

        private fun percentOf(part: Long, total: Long): Int =
            if (total == 0L) 0 else (part * 100 / total).toInt()
    }
}
