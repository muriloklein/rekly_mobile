package com.example.rekly.ui.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.rekly.R
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.databinding.FragmentSubscriptionsBinding
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.util.SessionManager

class SubscriptionsFragment : Fragment(R.layout.fragment_subscriptions) {

    // The view can be destroyed before the fragment itself; releasing the binding in
    // onDestroyView avoids holding a view that is no longer on screen.
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private var userId: Long = 0
    private val adapter = SubscriptionListAdapter()

    private val viewModel: SubscriptionsViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        SubscriptionsViewModel.factory(
            SubscriptionRepository(database.subscriptionDao(), database.paymentDao()),
            userId
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSubscriptionsBinding.bind(view)
        userId = SessionManager(requireContext()).loggedUserId() ?: return

        binding.subscriptionList.adapter = adapter
        binding.searchInput.doAfterTextChanged { viewModel.search(it?.toString().orEmpty()) }
        binding.statusChips.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.filterBy(statusOfChip(checkedIds.firstOrNull()))
        }
        viewModel.state.observe(viewLifecycleOwner) { render(it) }
    }

    private fun render(state: SubscriptionListState) {
        adapter.submitList(state.items)
        binding.emptyText.isVisible = state.items.isEmpty()
        binding.emptyText.setText(
            if (state.hasAnySubscription) R.string.subscriptions_no_results else R.string.subscriptions_empty
        )
    }

    private fun statusOfChip(chipId: Int?): SubscriptionStatus? = when (chipId) {
        R.id.chipActive -> SubscriptionStatus.ACTIVE
        R.id.chipTrial -> SubscriptionStatus.TRIAL
        R.id.chipCanceled -> SubscriptionStatus.CANCELED
        else -> null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
