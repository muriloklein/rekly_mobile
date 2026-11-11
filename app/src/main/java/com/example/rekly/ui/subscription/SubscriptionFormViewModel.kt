package com.example.rekly.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.entity.Category
import com.example.rekly.data.entity.Subscription
import com.example.rekly.data.repository.CategoryRepository
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.domain.catalog.ServiceCatalog
import com.example.rekly.domain.model.BillingPeriod
import com.example.rekly.domain.model.RuleError
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.domain.rules.Validators
import com.example.rekly.util.MoneyFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SubscriptionFormErrors(
    val serviceName: RuleError? = null,
    val category: RuleError? = null,
    val amount: RuleError? = null,
    val startDate: RuleError? = null,
    val billingDay: RuleError? = null,
    val participants: RuleError? = null
) {
    fun hasAny(): Boolean =
        listOf(serviceName, category, amount, startDate, billingDay, participants).any { it != null }
}

data class SubscriptionFormState(
    val categoryId: Long? = null,
    val period: BillingPeriod = BillingPeriod.MONTHLY,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val startDate: LocalDate? = null,
    val errors: SubscriptionFormErrors = SubscriptionFormErrors(),
    val saving: Boolean = false,
    val finished: Boolean = false
)

/** Values to write into the text fields once; a null field keeps what is on screen. */
data class SubscriptionPrefill(
    val serviceName: String? = null,
    val amountText: String? = null,
    val variableAmount: Boolean? = null,
    val billingDayText: String? = null,
    val participantsText: String? = null
)

class SubscriptionFormViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    categoryRepository: CategoryRepository,
    private val userId: Long,
    subscriptionId: Long?
) : ViewModel() {

    val categories: LiveData<List<Category>> = categoryRepository.observeByUser(userId)

    private val _state = MutableLiveData(SubscriptionFormState())
    val state: LiveData<SubscriptionFormState> = _state

    private val _prefill = MutableLiveData<SubscriptionPrefill?>()
    val prefill: LiveData<SubscriptionPrefill?> = _prefill

    private var editing: Subscription? = null

    init {
        if (subscriptionId == null) {
            _state.value = SubscriptionFormState(startDate = LocalDate.now())
            _prefill.value = SubscriptionPrefill(participantsText = "1")
        } else {
            viewModelScope.launch { load(subscriptionId) }
        }
    }

    fun onPrefillApplied() {
        _prefill.value = null
    }

    /**
     * Fills what the catalog knows (name, category, period, variable amount) and keeps
     * what the user already typed in the other fields (amount, billing day, participants).
     */
    fun applyCatalogService(serviceId: String) {
        val service = ServiceCatalog.findById(serviceId) ?: return
        update {
            it.copy(
                categoryId = service.categoryId,
                period = service.period,
                errors = it.errors.copy(serviceName = null, category = null)
            )
        }
        _prefill.value = SubscriptionPrefill(serviceName = service.name, variableAmount = service.variableAmount)
    }

    fun selectCategory(categoryId: Long) = update { it.copy(categoryId = categoryId) }

    fun selectPeriod(period: BillingPeriod) = update { it.copy(period = period) }

    fun selectStatus(status: SubscriptionStatus) = update { it.copy(status = status) }

    fun selectStartDate(startDate: LocalDate) = update { it.copy(startDate = startDate) }

    fun save(
        serviceName: String,
        amountText: String,
        variableAmount: Boolean,
        billingDayText: String,
        participantsText: String
    ) {
        val form = currentState()
        val amountCents = MoneyFormatter.parseCents(amountText)
        val billingDay = billingDayText.trim().toIntOrNull()
        val participants = participantsText.trim().toIntOrNull()

        val errors = SubscriptionFormErrors(
            serviceName = Validators.validateServiceName(serviceName),
            category = Validators.validateCategory(form.categoryId),
            amount = Validators.validateAmount(amountCents),
            startDate = Validators.validateStartDate(form.startDate, LocalDate.now()),
            billingDay = Validators.validateBillingDay(billingDay),
            participants = Validators.validateParticipants(participants)
        )
        val categoryId = form.categoryId
        if (errors.hasAny() || categoryId == null || participants == null) {
            _state.value = form.copy(errors = errors)
            return
        }

        _state.value = form.copy(errors = SubscriptionFormErrors(), saving = true)
        viewModelScope.launch {
            val base = editing ?: Subscription(
                userId = userId,
                categoryId = categoryId,
                serviceName = "",
                createdAt = LocalDate.now()
            )
            val subscription = base.copy(
                categoryId = categoryId,
                serviceName = serviceName.trim(),
                amountCents = amountCents,
                variableAmount = variableAmount,
                period = form.period,
                startDate = form.startDate,
                billingDay = billingDay,
                status = form.status,
                participants = participants
            )
            if (editing == null) {
                subscriptionRepository.create(subscription)
            } else {
                subscriptionRepository.update(subscription)
            }
            update { it.copy(saving = false, finished = true) }
        }
    }

    private suspend fun load(subscriptionId: Long) {
        val subscription = subscriptionRepository.findById(subscriptionId)
        if (subscription == null) {
            update { it.copy(finished = true) }
            return
        }
        editing = subscription
        _state.value = SubscriptionFormState(
            categoryId = subscription.categoryId,
            period = subscription.period,
            status = subscription.status,
            startDate = subscription.startDate ?: LocalDate.now()
        )
        _prefill.value = SubscriptionPrefill(
            serviceName = subscription.serviceName,
            amountText = subscription.amountCents?.let(MoneyFormatter::toInputText).orEmpty(),
            variableAmount = subscription.variableAmount,
            billingDayText = subscription.billingDay?.toString().orEmpty(),
            participantsText = subscription.participants.toString()
        )
    }

    private fun currentState(): SubscriptionFormState = _state.value ?: SubscriptionFormState()

    private fun update(change: (SubscriptionFormState) -> SubscriptionFormState) {
        _state.value = change(currentState())
    }

    companion object {
        fun factory(
            subscriptionRepository: SubscriptionRepository,
            categoryRepository: CategoryRepository,
            userId: Long,
            subscriptionId: Long?
        ) = viewModelFactory {
            initializer {
                SubscriptionFormViewModel(subscriptionRepository, categoryRepository, userId, subscriptionId)
            }
        }
    }
}
