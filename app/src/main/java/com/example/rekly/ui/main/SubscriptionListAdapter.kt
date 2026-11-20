package com.example.rekly.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rekly.R
import com.example.rekly.data.entity.SubscriptionWithCategory
import com.example.rekly.databinding.ItemSubscriptionBinding
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.domain.rules.ExpenseCalculator
import com.example.rekly.util.ChipTone
import com.example.rekly.util.DateFormatter
import com.example.rekly.util.chipTone
import com.example.rekly.util.labelRes
import com.example.rekly.util.monthlyAmountText
import com.example.rekly.util.showCategoryAvatar
import com.example.rekly.util.showChip
import java.time.LocalDate

class SubscriptionListAdapter(
    private val onItemClick: (subscriptionId: Long) -> Unit
) : ListAdapter<SubscriptionWithCategory, SubscriptionListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SubscriptionWithCategory) {
            val context = binding.root.context
            val subscription = item.subscription
            val monthlyCents = ExpenseCalculator.monthlyCents(subscription).takeIf { subscription.isComplete() }

            binding.categoryIcon.showCategoryAvatar(item.category.id)
            binding.nameText.text = subscription.serviceName
            binding.categoryText.text = item.category.name

            // Active is the normal case, so only the exceptions get a chip
            if (subscription.status == SubscriptionStatus.ACTIVE) {
                binding.statusChip.isVisible = false
            } else {
                binding.statusChip.showChip(subscription.status.labelRes(), subscription.status.chipTone())
            }

            val nextDueDate = monthlyCents?.let { ExpenseCalculator.nextDueDate(subscription, LocalDate.now()) }
            binding.amountText.isVisible = monthlyCents != null
            binding.dueText.isVisible = nextDueDate != null
            if (monthlyCents != null) {
                binding.amountText.text = context.monthlyAmountText(monthlyCents, SUFFIX_SIZE)
                nextDueDate?.let {
                    binding.dueText.text = context.getString(R.string.subscription_due, DateFormatter.formatDayMonth(it))
                }
                binding.missingAmountChip.isVisible = false
            } else {
                binding.missingAmountChip.showChip(R.string.subscription_missing_amount, ChipTone.ERROR)
            }
            binding.root.setOnClickListener { onItemClick(subscription.id) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<SubscriptionWithCategory>() {
        override fun areItemsTheSame(old: SubscriptionWithCategory, new: SubscriptionWithCategory): Boolean =
            old.subscription.id == new.subscription.id

        override fun areContentsTheSame(old: SubscriptionWithCategory, new: SubscriptionWithCategory): Boolean =
            old == new
    }

    private companion object {
        const val SUFFIX_SIZE = 0.7f
    }
}
