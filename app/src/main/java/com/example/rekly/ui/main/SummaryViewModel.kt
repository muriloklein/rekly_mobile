package com.example.rekly.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.repository.PaymentRepository
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.domain.model.OperationResult
import com.example.rekly.domain.model.RuleError
import kotlinx.coroutines.launch
import java.time.LocalDate

class SummaryViewModel(
    subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
    userId: Long
) : ViewModel() {

    private val subscriptions = subscriptionRepository.observeByUser(userId)
    private val pendingPayments = paymentRepository.observePending(userId)

    /**
     * Combines the two Room sources: whenever a subscription or a pending payment changes
     * (in any screen), the summary is recalculated by itself, with no manual reload.
     */
    val state: LiveData<SummaryState> = MediatorLiveData<SummaryState>().apply {
        val recalculate = {
            val currentSubscriptions = subscriptions.value
            val currentPending = pendingPayments.value
            if (currentSubscriptions != null && currentPending != null) {
                value = SummaryState.from(currentSubscriptions, currentPending, LocalDate.now())
            }
        }
        addSource(subscriptions) { recalculate() }
        addSource(pendingPayments) { recalculate() }
    }

    private val _error = MutableLiveData<RuleError?>()
    val error: LiveData<RuleError?> = _error

    /** Only a pending payment of this user, currently on screen, can be confirmed. */
    fun confirmPayment(paymentId: Long, amountCents: Long) {
        val payment = pendingPayments.value?.firstOrNull { it.id == paymentId } ?: return
        viewModelScope.launch {
            val result = paymentRepository.confirm(payment, amountCents)
            if (result is OperationResult.Failure) _error.value = result.error
        }
    }

    fun onErrorShown() {
        _error.value = null
    }

    companion object {
        fun factory(
            subscriptionRepository: SubscriptionRepository,
            paymentRepository: PaymentRepository,
            userId: Long
        ) = viewModelFactory {
            initializer { SummaryViewModel(subscriptionRepository, paymentRepository, userId) }
        }
    }
}
