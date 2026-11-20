package com.example.rekly.ui.subscription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.rekly.R
import com.example.rekly.data.entity.Payment
import com.example.rekly.data.entity.Subscription
import com.example.rekly.data.entity.SubscriptionDetails
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.repository.PaymentRepository
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.databinding.ActivitySubscriptionDetailsBinding
import com.example.rekly.databinding.LayoutDetailRowBinding
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.domain.rules.ExpenseCalculator
import com.example.rekly.ui.auth.LoginActivity
import com.example.rekly.util.DateFormatter
import com.example.rekly.util.MoneyFormatter
import com.example.rekly.util.SessionManager
import com.example.rekly.util.applyFormScreenInsets
import com.example.rekly.util.chipTone
import com.example.rekly.util.labelRes
import com.example.rekly.util.messageRes
import com.example.rekly.util.monthlyAmountText
import com.example.rekly.util.showCategoryAvatar
import com.example.rekly.util.showChip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate

class SubscriptionDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionDetailsBinding
    private var userId: Long = 0

    private val subscriptionId: Long
        get() = intent.getLongExtra(EXTRA_SUBSCRIPTION_ID, NO_ID)

    private val paymentAdapter = PaymentAdapter { payment -> openConfirmPayment(payment) }

    private val viewModel: SubscriptionDetailsViewModel by viewModels {
        val database = AppDatabase.getInstance(this)
        SubscriptionDetailsViewModel.factory(
            SubscriptionRepository(database.subscriptionDao(), database.paymentDao()),
            PaymentRepository(database.subscriptionDao(), database.paymentDao()),
            userId,
            subscriptionId
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loggedUserId = SessionManager(this).loggedUserId()
        if (loggedUserId == null) {
            openLogin()
            return
        }
        if (subscriptionId == NO_ID) {
            finish()
            return
        }
        userId = loggedUserId

        enableEdgeToEdge()
        binding = ActivitySubscriptionDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyFormScreenInsets(binding.root, binding.toolbar.root)
        setUpLabels()
        binding.paymentsList.adapter = paymentAdapter
        supportFragmentManager.setFragmentResultListener(ConfirmPaymentBottomSheet.REQUEST_KEY, this) { _, result ->
            viewModel.confirmPayment(
                ConfirmPaymentBottomSheet.paymentId(result),
                ConfirmPaymentBottomSheet.amountCents(result)
            )
        }

        binding.completeButton.setOnClickListener { openEdit() }
        binding.cancelButton.setOnClickListener { confirmCancel() }
        binding.deleteButton.setOnClickListener { confirmDelete() }

        viewModel.details.observe(this) { details ->
            if (details == null) finish() else render(details)
        }
        viewModel.error.observe(this) { error ->
            error ?: return@observe
            Snackbar.make(binding.root, error.messageRes(), Snackbar.LENGTH_LONG).show()
            viewModel.onErrorShown()
        }
    }

    override fun onStart() {
        super.onStart()
        if (SessionManager(this).loggedUserId() == null) openLogin()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_subscription_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_edit) {
            openEdit()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setUpLabels() {
        binding.amountRow.labelText.setText(R.string.label_amount)
        binding.realCostRow.labelText.setText(R.string.label_real_cost)
        binding.nextDueDateRow.labelText.setText(R.string.label_next_due_date)
        binding.startDateRow.labelText.setText(R.string.label_start_date)
        binding.billingDayRow.labelText.setText(R.string.label_billing_day)
    }

    private fun render(details: SubscriptionDetails) {
        val subscription = details.subscription
        supportActionBar?.title = subscription.serviceName

        binding.categoryIcon.showCategoryAvatar(details.category.id)
        binding.categoryText.text = details.category.name
        binding.statusChip.showChip(subscription.status.labelRes(), subscription.status.chipTone())

        binding.incompleteCard.isVisible = !subscription.isComplete()
        binding.numbersGroup.isVisible = subscription.isComplete()
        binding.monthlyLabel.isVisible = subscription.isComplete()
        binding.monthlyAmountText.isVisible = subscription.isComplete()
        if (subscription.isComplete()) renderNumbers(subscription)

        binding.cancelButton.isVisible = subscription.status != SubscriptionStatus.CANCELED

        paymentAdapter.submitList(details.payments.sortedByDescending { it.referenceMonth })
        binding.paymentsEmptyText.isVisible = details.payments.isEmpty()
        binding.paymentsList.isVisible = details.payments.isNotEmpty()
    }

    private fun renderNumbers(subscription: Subscription) {
        val amountCents = subscription.amountCents ?: return
        val startDate = subscription.startDate ?: return
        val billingDay = subscription.billingDay ?: return
        val monthlyCents = ExpenseCalculator.monthlyCents(amountCents, subscription.period)
        val realCost = MoneyFormatter.format(
            ExpenseCalculator.realCostCents(amountCents, subscription.period, subscription.participants)
        )

        binding.amountRow.valueText.text =
            getString(R.string.detail_amount_period, MoneyFormatter.format(amountCents), periodText(subscription))
        binding.monthlyAmountText.text = monthlyAmountText(monthlyCents, SUFFIX_SIZE)
        binding.realCostRow.valueText.text = if (subscription.participants > 1) {
            getString(R.string.detail_real_cost_split, realCost, subscription.participants)
        } else {
            realCost
        }
        binding.nextDueDateRow.valueText.text = ExpenseCalculator.nextDueDate(subscription, LocalDate.now())
            ?.let(DateFormatter::format)
            ?: getString(R.string.detail_no_due_date)
        binding.startDateRow.valueText.text = DateFormatter.format(startDate)
        binding.billingDayRow.valueText.text = getString(R.string.detail_billing_day, billingDay)
    }

    private fun periodText(subscription: Subscription): String {
        val period = getString(subscription.period.labelRes())
        return if (subscription.variableAmount) getString(R.string.catalog_detail_variable, period) else period
    }

    private fun confirmCancel() = confirm(
        title = R.string.dialog_cancel_title,
        message = R.string.dialog_cancel_message,
        action = R.string.action_cancel_subscription
    ) { viewModel.cancel() }

    private fun confirmDelete() = confirm(
        title = R.string.dialog_delete_title,
        message = R.string.dialog_delete_message,
        action = R.string.action_delete
    ) { viewModel.delete() }

    private fun confirm(@StringRes title: Int, @StringRes message: Int, @StringRes action: Int, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(R.string.action_back, null)
            .setPositiveButton(action) { _, _ -> onConfirm() }
            .show()
    }

    private fun openConfirmPayment(payment: Payment) {
        val month = DateFormatter.formatMonth(payment.referenceMonth)
        ConfirmPaymentBottomSheet.newInstance(payment.id, month, payment.amountCents)
            .show(supportFragmentManager, ConfirmPaymentBottomSheet.TAG)
    }

    private fun openEdit() {
        startActivity(SubscriptionFormActivity.newIntent(this, subscriptionId))
    }

    private fun openLogin() {
        startActivity(LoginActivity.newTask(this))
        finish()
    }

    companion object {
        private const val EXTRA_SUBSCRIPTION_ID = "subscription_id"
        private const val NO_ID = -1L
        private const val SUFFIX_SIZE = 0.5f

        fun newIntent(context: Context, subscriptionId: Long): Intent =
            Intent(context, SubscriptionDetailsActivity::class.java)
                .putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
    }
}
