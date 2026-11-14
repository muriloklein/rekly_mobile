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
import com.example.rekly.domain.rules.ExpenseCalculator
import com.example.rekly.util.MoneyFormatter
import com.example.rekly.util.labelRes

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
            val status = context.getString(subscription.status.labelRes())
            val monthlyCents = ExpenseCalculator.monthlyCents(subscription).takeIf { subscription.isComplete() }

            binding.nameText.text = subscription.serviceName
            binding.detailText.text = context.getString(R.string.subscription_item_detail, item.category.name, status)
            binding.amountText.isVisible = monthlyCents != null
            binding.missingAmountText.isVisible = monthlyCents == null
            monthlyCents?.let {
                binding.amountText.text = context.getString(R.string.subscription_monthly_amount, MoneyFormatter.format(it))
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
}
