package com.example.rekly.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.data.repository.UserRepository
import com.example.rekly.domain.catalog.ServiceCatalog
import kotlinx.coroutines.launch
import java.time.LocalDate

data class OnboardingState(
    val selectedIds: Set<String> = emptySet(),
    val saving: Boolean = false,
    val finished: Boolean = false
)

class OnboardingViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository,
    private val userId: Long
) : ViewModel() {

    private val _state = MutableLiveData(OnboardingState())
    val state: LiveData<OnboardingState> = _state

    fun toggle(serviceId: String, selected: Boolean) {
        val current = currentState()
        val ids = if (selected) current.selectedIds + serviceId else current.selectedIds - serviceId
        _state.value = current.copy(selectedIds = ids)
    }

    fun finish() = complete(currentState().selectedIds)

    fun skip() = complete(emptySet())

    private fun complete(serviceIds: Set<String>) {
        if (currentState().saving) return
        _state.value = currentState().copy(saving = true)
        viewModelScope.launch {
            val subscriptions = ServiceCatalog.toSubscriptions(serviceIds, userId, LocalDate.now())
            if (subscriptions.isNotEmpty()) subscriptionRepository.createAll(subscriptions)
            userRepository.completeOnboarding(userId)
            _state.value = currentState().copy(saving = false, finished = true)
        }
    }

    private fun currentState(): OnboardingState = _state.value ?: OnboardingState()

    companion object {
        fun factory(subscriptionRepository: SubscriptionRepository, userRepository: UserRepository, userId: Long) =
            viewModelFactory {
                initializer { OnboardingViewModel(subscriptionRepository, userRepository, userId) }
            }
    }
}
