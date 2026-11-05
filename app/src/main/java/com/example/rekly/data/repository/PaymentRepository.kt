package com.example.rekly.data.repository

import com.example.rekly.data.entity.Payment
import com.example.rekly.data.local.PaymentDao
import com.example.rekly.data.local.SubscriptionDao
import com.example.rekly.domain.model.OperationResult
import com.example.rekly.domain.model.PaymentStatus
import com.example.rekly.domain.model.RuleError
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.domain.rules.PaymentGenerator
import java.time.LocalDate

class PaymentRepository(
    private val subscriptionDao: SubscriptionDao,
    private val paymentDao: PaymentDao
) {

    /**
     * Generates the presumed payments for all of the user's active subscriptions.
     * Called when the MainActivity opens. Returns how many payments were created.
     */
    suspend fun generatePresumed(userId: Long, today: LocalDate = LocalDate.now()): Int {
        val newPayments = subscriptionDao.listByStatus(userId, SubscriptionStatus.ACTIVE).flatMap { subscription ->
            PaymentGenerator.generate(
                subscription = subscription,
                existingMonths = paymentDao.listReferenceMonths(subscription.id).toSet(),
                lastPaidAmountCents = paymentDao.lastAmount(subscription.id, PaymentStatus.PAID),
                today = today
            )
        }
        if (newPayments.isEmpty()) return 0
        return paymentDao.insertAll(newPayments).count { it != -1L }
    }

    /** Confirms a pending payment with the real amount, which must be greater than zero. */
    suspend fun confirm(
        payment: Payment,
        amountCents: Long,
        today: LocalDate = LocalDate.now()
    ): OperationResult<Unit> {
        if (amountCents <= 0) return OperationResult.Failure(RuleError.INVALID_AMOUNT)
        paymentDao.update(
            payment.copy(
                amountCents = amountCents,
                paidDate = today,
                status = PaymentStatus.PAID
            )
        )
        return OperationResult.Success(Unit)
    }
}
