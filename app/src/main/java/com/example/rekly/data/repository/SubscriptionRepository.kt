package com.example.rekly.data.repository

import androidx.lifecycle.LiveData
import com.example.rekly.data.entity.Subscription
import com.example.rekly.data.entity.SubscriptionDetails
import com.example.rekly.data.entity.SubscriptionWithCategory
import com.example.rekly.data.local.PaymentDao
import com.example.rekly.data.local.SubscriptionDao
import com.example.rekly.domain.model.OperationResult
import com.example.rekly.domain.model.RuleError
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.domain.rules.PaymentGenerator
import java.time.LocalDate

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val paymentDao: PaymentDao
) {

    fun observeByUser(userId: Long): LiveData<List<SubscriptionWithCategory>> =
        subscriptionDao.observeWithCategory(userId)

    fun observeDetails(id: Long): LiveData<SubscriptionDetails?> =
        subscriptionDao.observeDetails(id)

    suspend fun findById(id: Long): Subscription? = subscriptionDao.findById(id)

    suspend fun create(subscription: Subscription, today: LocalDate = LocalDate.now()): Long =
        subscriptionDao.insert(PaymentGenerator.adjustActivation(null, subscription, today))

    /** Batch creation from the onboarding (subscriptions still without an amount). */
    suspend fun createAll(subscriptions: List<Subscription>, today: LocalDate = LocalDate.now()): List<Long> =
        subscriptionDao.insertAll(subscriptions.map { PaymentGenerator.adjustActivation(null, it, today) })

    suspend fun update(subscription: Subscription, today: LocalDate = LocalDate.now()): OperationResult<Unit> {
        val previous = subscriptionDao.findById(subscription.id)
            ?: return OperationResult.Failure(RuleError.NOT_FOUND)
        subscriptionDao.update(PaymentGenerator.adjustActivation(previous, subscription, today))
        return OperationResult.Success(Unit)
    }

    /** Canceling keeps the history and stops generating new payments. */
    suspend fun cancel(subscription: Subscription): OperationResult<Unit> =
        update(subscription.copy(status = SubscriptionStatus.CANCELED))

    /** Deletion is blocked if there are payments; in that case the alternative is to cancel. */
    suspend fun delete(subscription: Subscription): OperationResult<Unit> {
        if (paymentDao.countBySubscription(subscription.id) > 0) {
            return OperationResult.Failure(RuleError.SUBSCRIPTION_HAS_PAYMENTS)
        }
        subscriptionDao.delete(subscription)
        return OperationResult.Success(Unit)
    }
}
