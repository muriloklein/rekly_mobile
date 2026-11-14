package com.example.rekly.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.entity.Subscription
import com.example.rekly.data.entity.SubscriptionDetails
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.domain.model.OperationResult
import com.example.rekly.domain.model.RuleError
import kotlinx.coroutines.launch

class SubscriptionDetailsViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    userId: Long,
    subscriptionId: Long
) : ViewModel() {

    val details: LiveData<SubscriptionDetails?> =
        subscriptionRepository.observeDetails(subscriptionId).map { details ->
            details?.takeIf { it.subscription.userId == userId }
        }

    private val _error = MutableLiveData<RuleError?>()
    val error: LiveData<RuleError?> = _error

    fun cancel() = runOnCurrent { subscriptionRepository.cancel(it) }

    fun delete() = runOnCurrent { subscriptionRepository.delete(it) }

    fun onErrorShown() {
        _error.value = null
    }

    private fun runOnCurrent(operation: suspend (Subscription) -> OperationResult<Unit>) {
        val subscription = details.value?.subscription ?: return
        viewModelScope.launch {
            val result = operation(subscription)
            if (result is OperationResult.Failure) _error.value = result.error
        }
    }

    companion object {
        fun factory(subscriptionRepository: SubscriptionRepository, userId: Long, subscriptionId: Long) =
            viewModelFactory {
                initializer { SubscriptionDetailsViewModel(subscriptionRepository, userId, subscriptionId) }
            }
    }
}
