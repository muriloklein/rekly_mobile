package com.example.rekly.ui.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rekly.R
import com.example.rekly.data.entity.Payment
import com.example.rekly.databinding.ItemPaymentBinding
import com.example.rekly.domain.model.PaymentStatus
import com.example.rekly.util.DateFormatter
import com.example.rekly.util.MoneyFormatter
import com.example.rekly.util.chipTone
import com.example.rekly.util.labelRes
import com.example.rekly.util.showChip

class PaymentAdapter(
    private val onAdjustClick: (Payment) -> Unit,
    private val onTogglePaidClick: (Payment) -> Unit
) : ListAdapter<Payment, PaymentAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPaymentBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: Payment) {
            val context = binding.root.context
            val amount = MoneyFormatter.format(payment.amountCents)
            val isPaid = payment.status == PaymentStatus.PAID

            binding.monthText.text = DateFormatter.formatMonth(payment.referenceMonth)
            binding.statusChip.showChip(payment.status.labelRes(), payment.status.chipTone())
            binding.amountText.text = if (isPaid) amount else context.getString(R.string.payment_amount_estimate, amount)
            binding.detailText.text = if (isPaid) {
                context.getString(R.string.payment_paid_on, DateFormatter.format(payment.paidDate ?: payment.dueDate))
            } else {
                context.getString(R.string.payment_due_on, DateFormatter.format(payment.dueDate))
            }
            binding.adjustButton.isVisible = !isPaid
            binding.adjustButton.setOnClickListener { onAdjustClick(payment) }
            binding.togglePaidButton.setText(if (isPaid) R.string.action_mark_unpaid else R.string.action_mark_paid)
            binding.togglePaidButton.setOnClickListener { onTogglePaidClick(payment) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(old: Payment, new: Payment): Boolean = old.id == new.id

        override fun areContentsTheSame(old: Payment, new: Payment): Boolean = old == new
    }
}
