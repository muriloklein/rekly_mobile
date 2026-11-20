package com.example.rekly.ui.main

import android.content.res.ColorStateList
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
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
import com.example.rekly.util.MoneyFormatter
import com.example.rekly.util.categoryVisual
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
            val visual = categoryVisual(item.category.id)
            val monthlyCents = ExpenseCalculator.monthlyCents(subscription).takeIf { subscription.isComplete() }

            binding.categoryIcon.setImageResource(visual.icon)
            binding.categoryIcon.backgroundTintList = ColorStateList.valueOf(context.getColor(visual.container))
            binding.categoryIcon.imageTintList = ColorStateList.valueOf(context.getColor(visual.onContainer))
            binding.nameText.text = subscription.serviceName
            binding.categoryText.text = item.category.name

            // Active is the normal case, so only the exceptions get a chip
            when (subscription.status) {
                SubscriptionStatus.TRIAL -> binding.statusChip.showChip(R.string.status_trial, ChipTone.ATTENTION)
                SubscriptionStatus.CANCELED -> binding.statusChip.showChip(R.string.status_canceled, ChipTone.NEUTRAL)
                SubscriptionStatus.ACTIVE -> binding.statusChip.isVisible = false
            }

            val nextDueDate = monthlyCents?.let { ExpenseCalculator.nextDueDate(subscription, LocalDate.now()) }
            binding.amountText.isVisible = monthlyCents != null
            binding.dueText.isVisible = nextDueDate != null
            if (monthlyCents != null) {
                binding.amountText.text = buildSpannedString {
                    append(MoneyFormatter.format(monthlyCents))
                    inSpans(RelativeSizeSpan(SUFFIX_SIZE)) { append(context.getString(R.string.subscription_per_month)) }
                }
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
