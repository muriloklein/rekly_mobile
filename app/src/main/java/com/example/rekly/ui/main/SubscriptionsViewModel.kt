package com.example.rekly.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.entity.SubscriptionWithCategory
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.domain.rules.SubscriptionFilter

data class SubscriptionListState(
    val items: List<SubscriptionWithCategory>,
    val hasAnySubscription: Boolean
)

class SubscriptionsViewModel(
    subscriptionRepository: SubscriptionRepository,
    userId: Long
) : ViewModel() {

    private val allSubscriptions = subscriptionRepository.observeByUser(userId)
    private val query = MutableLiveData("")
    private val status = MutableLiveData<SubscriptionStatus?>(null)

    private val _state = MediatorLiveData<SubscriptionListState>()
    val state: LiveData<SubscriptionListState> = _state

    init {
        _state.addSource(allSubscriptions) { refresh() }
        _state.addSource(query) { refresh() }
        _state.addSource(status) { refresh() }
    }

    fun search(text: String) {
        if (query.value != text) query.value = text
    }

    fun filterBy(selectedStatus: SubscriptionStatus?) {
        if (status.value != selectedStatus) status.value = selectedStatus
    }

    private fun refresh() {
        val subscriptions = allSubscriptions.value ?: return
        val currentQuery = query.value.orEmpty()
        val currentStatus = status.value
        _state.value = SubscriptionListState(
            items = subscriptions.filter { SubscriptionFilter.matches(it.subscription, currentQuery, currentStatus) },
            hasAnySubscription = subscriptions.isNotEmpty()
        )
    }

    companion object {
        fun factory(subscriptionRepository: SubscriptionRepository, userId: Long) = viewModelFactory {
            initializer { SubscriptionsViewModel(subscriptionRepository, userId) }
        }
    }
}
