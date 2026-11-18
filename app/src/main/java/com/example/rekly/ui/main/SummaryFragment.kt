package com.example.rekly.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rekly.R
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.repository.PaymentRepository
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.databinding.FragmentSummaryBinding
import com.example.rekly.databinding.ItemSummaryCategoryBinding
import com.example.rekly.databinding.ItemSummaryPendingBinding
import com.example.rekly.databinding.ItemSummarySuggestionBinding
import com.example.rekly.databinding.ItemSummaryUpcomingBinding
import com.example.rekly.domain.model.SavingsSuggestion
import com.example.rekly.ui.subscription.ConfirmPaymentBottomSheet
import com.example.rekly.ui.subscription.SubscriptionDetailsActivity
import com.example.rekly.util.DateFormatter
import com.example.rekly.util.MoneyFormatter
import com.example.rekly.util.SessionManager
import com.example.rekly.util.messageRes
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate

/**
 * Home tab: monthly totals, due dates of the next 7 days, pending bills, spending by
 * category and savings suggestions. Everything comes calculated from [SummaryViewModel];
 * this screen only formats and shows it.
 *
 * The lists are short, so their rows are inflated straight into LinearLayouts instead of
 * nesting RecyclerViews inside the scroll.
 */
class SummaryFragment : Fragment(R.layout.fragment_summary) {

    // The view can be destroyed before the fragment itself; releasing the binding in
    // onDestroyView avoids holding a view that is no longer on screen.
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private var userId: Long = 0

    private val viewModel: SummaryViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        SummaryViewModel.factory(
            SubscriptionRepository(database.subscriptionDao(), database.paymentDao()),
            PaymentRepository(database.subscriptionDao(), database.paymentDao()),
            userId
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSummaryBinding.bind(view)
        userId = SessionManager(requireContext()).loggedUserId() ?: return

        // The confirm panel is a child of this fragment, so its result arrives here,
        // even after a rotation with the panel open
        childFragmentManager.setFragmentResultListener(ConfirmPaymentBottomSheet.REQUEST_KEY, viewLifecycleOwner) { _, result ->
            viewModel.confirmPayment(
                ConfirmPaymentBottomSheet.paymentId(result),
                ConfirmPaymentBottomSheet.amountCents(result)
            )
        }
        viewModel.state.observe(viewLifecycleOwner) { render(it) }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error ?: return@observe
            Snackbar.make(binding.root, error.messageRes(), Snackbar.LENGTH_LONG).show()
            viewModel.onErrorShown()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun render(state: SummaryState) {
        binding.emptyText.isVisible = !state.hasSubscriptions
        binding.contentScroll.isVisible = state.hasSubscriptions
        if (!state.hasSubscriptions) return

        renderTotals(state)
        renderUpcoming(state.upcoming)
        renderPending(state.pending)
        renderCategories(state.spendingByCategory)
        renderSuggestions(state.suggestions, state.categoryNames)
    }

    private fun renderTotals(state: SummaryState) {
        binding.monthlyTotalText.text = MoneyFormatter.format(state.monthlyTotalCents)
        binding.realCostText.text = getString(R.string.summary_real_cost, MoneyFormatter.format(state.realCostTotalCents))

        binding.trialText.isVisible = state.trialCount > 0
        binding.trialText.text = resources.getQuantityString(
            R.plurals.summary_trial,
            state.trialCount,
            state.trialCount,
            MoneyFormatter.format(state.trialMonthlyCents)
        )

        binding.incompleteText.isVisible = state.incompleteCount > 0
        binding.incompleteText.text = resources.getQuantityString(
            R.plurals.summary_incomplete,
            state.incompleteCount,
            state.incompleteCount
        )
    }

    private fun renderUpcoming(items: List<SummaryState.UpcomingDueDate>) {
        binding.upcomingList.removeAllViews()
        binding.upcomingEmptyText.isVisible = items.isEmpty()
        items.forEach { item ->
            val row = ItemSummaryUpcomingBinding.inflate(layoutInflater, binding.upcomingList, true)
            val amount = MoneyFormatter.format(item.amountCents)
            row.nameText.text = item.serviceName
            row.dueText.text = dueLabel(item.dueDate)
            row.amountText.text = if (item.variableAmount) getString(R.string.payment_amount_estimate, amount) else amount
            row.root.setOnClickListener { openDetails(item.subscriptionId) }
        }
    }

    private fun renderPending(items: List<SummaryState.PendingPayment>) {
        binding.pendingSection.isVisible = items.isNotEmpty()
        binding.pendingList.removeAllViews()
        items.forEach { item ->
            val row = ItemSummaryPendingBinding.inflate(layoutInflater, binding.pendingList, true)
            val estimate = getString(R.string.payment_amount_estimate, MoneyFormatter.format(item.estimateCents))
            row.nameText.text = item.serviceName
            row.detailText.text = getString(
                R.string.summary_pending_detail,
                DateFormatter.formatMonth(item.referenceMonth),
                estimate
            )
            row.confirmButton.setOnClickListener { openConfirmPayment(item) }
            row.root.setOnClickListener { openDetails(item.subscriptionId) }
        }
    }

    private fun renderCategories(items: List<SummaryState.CategorySpending>) {
        binding.categoriesSection.isVisible = items.isNotEmpty()
        binding.categoryList.removeAllViews()
        items.forEach { item ->
            val row = ItemSummaryCategoryBinding.inflate(layoutInflater, binding.categoryList, true)
            row.nameText.text = item.categoryName
            row.amountText.text = getString(R.string.summary_category_share, MoneyFormatter.format(item.monthlyCents), item.percent)
            row.shareIndicator.progress = item.percent
        }
    }

    private fun renderSuggestions(items: List<SavingsSuggestion>, categoryNames: Map<Long, String>) {
        binding.suggestionsSection.isVisible = items.isNotEmpty()
        binding.suggestionList.removeAllViews()
        items.forEach { suggestion ->
            val row = ItemSummarySuggestionBinding.inflate(layoutInflater, binding.suggestionList, true)
            row.suggestionText.text = suggestionText(suggestion, categoryNames)
            subscriptionOf(suggestion)?.let { id -> row.root.setOnClickListener { openDetails(id) } }
        }
    }

    private fun suggestionText(suggestion: SavingsSuggestion, categoryNames: Map<Long, String>): String =
        when (suggestion) {
            is SavingsSuggestion.Overlap -> getString(
                R.string.suggestion_overlap,
                suggestion.serviceNames.size,
                categoryNames[suggestion.categoryId].orEmpty(),
                suggestion.serviceNames.joinToString(", ")
            )
            is SavingsSuggestion.LongTrial ->
                getString(R.string.suggestion_long_trial, suggestion.serviceName, suggestion.days)
            is SavingsSuggestion.StalePending ->
                getString(R.string.suggestion_stale_pending, suggestion.serviceName, suggestion.days)
        }

    /** Overlap involves several subscriptions, so it does not open a single details screen. */
    private fun subscriptionOf(suggestion: SavingsSuggestion): Long? = when (suggestion) {
        is SavingsSuggestion.Overlap -> null
        is SavingsSuggestion.LongTrial -> suggestion.subscriptionId
        is SavingsSuggestion.StalePending -> suggestion.subscriptionId
    }

    private fun dueLabel(dueDate: LocalDate): String {
        val today = LocalDate.now()
        return when (dueDate) {
            today -> getString(R.string.summary_due_today)
            today.plusDays(1) -> getString(R.string.summary_due_tomorrow)
            else -> getString(R.string.payment_due_on, DateFormatter.format(dueDate))
        }
    }

    private fun openConfirmPayment(item: SummaryState.PendingPayment) {
        if (childFragmentManager.findFragmentByTag(ConfirmPaymentBottomSheet.TAG) != null) return
        ConfirmPaymentBottomSheet.newInstance(
            item.paymentId,
            DateFormatter.formatMonth(item.referenceMonth),
            item.estimateCents
        ).show(childFragmentManager, ConfirmPaymentBottomSheet.TAG)
    }

    private fun openDetails(subscriptionId: Long) {
        startActivity(SubscriptionDetailsActivity.newIntent(requireContext(), subscriptionId))
    }
}
