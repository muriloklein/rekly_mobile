package com.example.rekly.ui.subscription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.rekly.R
import com.example.rekly.data.entity.Category
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.repository.CategoryRepository
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.databinding.ActivitySubscriptionFormBinding
import com.example.rekly.domain.model.BillingPeriod
import com.example.rekly.domain.model.RuleError
import com.example.rekly.domain.model.SubscriptionStatus
import com.example.rekly.ui.auth.LoginActivity
import com.example.rekly.util.DateFormatter
import com.example.rekly.util.SessionManager
import com.example.rekly.util.applyFormScreenInsets
import com.example.rekly.util.labelRes
import com.example.rekly.util.messageRes
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate

class SubscriptionFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionFormBinding
    private var userId: Long = 0
    private var categories: List<Category> = emptyList()

    private val subscriptionId: Long?
        get() = intent.getLongExtra(EXTRA_SUBSCRIPTION_ID, NO_ID).takeIf { it != NO_ID }

    private val viewModel: SubscriptionFormViewModel by viewModels {
        val database = AppDatabase.getInstance(this)
        SubscriptionFormViewModel.factory(
            SubscriptionRepository(database.subscriptionDao(), database.paymentDao()),
            CategoryRepository(database.categoryDao(), database.subscriptionDao()),
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
        userId = loggedUserId

        enableEdgeToEdge()
        binding = ActivitySubscriptionFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(
            if (subscriptionId == null) R.string.title_new_subscription else R.string.title_edit_subscription
        )
        applyFormScreenInsets(binding.root, binding.toolbar.root)

        setUpChoiceFields()
        setUpCatalog(savedInstanceState)
        binding.saveButton.setOnClickListener { save() }

        viewModel.categories.observe(this) { list ->
            categories = list
            binding.categoryInput.setSimpleItems(list.map { it.name }.toTypedArray())
            viewModel.state.value?.let { renderChoices(it) }
        }
        viewModel.state.observe(this) { render(it) }
        viewModel.prefill.observe(this) { prefill ->
            prefill ?: return@observe
            applyPrefill(prefill)
            viewModel.onPrefillApplied()
        }
    }

    override fun onStart() {
        super.onStart()
        if (SessionManager(this).loggedUserId() == null) openLogin()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setUpChoiceFields() {
        binding.categoryInput.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectCategory(categories[position].id)
        }

        binding.periodInput.setSimpleItems(BillingPeriod.entries.map { getString(it.labelRes()) }.toTypedArray())
        binding.periodInput.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectPeriod(BillingPeriod.entries[position])
        }

        binding.statusInput.setSimpleItems(SubscriptionStatus.entries.map { getString(it.labelRes()) }.toTypedArray())
        binding.statusInput.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectStatus(SubscriptionStatus.entries[position])
        }

        binding.startDateInput.setOnClickListener { openDatePicker() }
        // After a rotation the picker is restored by the FragmentManager, but without its listener
        findDatePicker()?.let { listenToDatePicker(it) }
    }

    private fun save() {
        viewModel.save(
            serviceName = binding.serviceNameInput.text?.toString().orEmpty(),
            amountText = binding.amountInput.text?.toString().orEmpty(),
            variableAmount = binding.variableAmountSwitch.isChecked,
            billingDayText = binding.billingDayInput.text?.toString().orEmpty(),
            participantsText = binding.participantsInput.text?.toString().orEmpty()
        )
    }

    private fun render(state: SubscriptionFormState) {
        binding.serviceNameLayout.error = state.errors.serviceName?.message()
        binding.categoryLayout.error = state.errors.category?.message()
        binding.amountLayout.error = state.errors.amount?.message()
        binding.startDateLayout.error = state.errors.startDate?.message()
        binding.billingDayLayout.error = state.errors.billingDay?.message()
        binding.participantsLayout.error = state.errors.participants?.message()
        binding.saveButton.isEnabled = !state.saving
        renderChoices(state)

        if (state.finished) finish()
    }

    private fun renderChoices(state: SubscriptionFormState) {
        val categoryName = categories.firstOrNull { it.id == state.categoryId }?.name.orEmpty()
        binding.categoryInput.setText(categoryName, false)
        binding.periodInput.setText(getString(state.period.labelRes()), false)
        binding.statusInput.setText(getString(state.status.labelRes()), false)
        binding.startDateInput.setText(state.startDate?.let(DateFormatter::format).orEmpty())
    }

    private fun applyPrefill(prefill: SubscriptionPrefill) {
        prefill.serviceName?.let { binding.serviceNameInput.setText(it) }
        prefill.amountText?.let { binding.amountInput.setText(it) }
        prefill.variableAmount?.let { binding.variableAmountSwitch.isChecked = it }
        prefill.billingDayText?.let { binding.billingDayInput.setText(it) }
        prefill.participantsText?.let { binding.participantsInput.setText(it) }
    }

    /**
     * The catalog is only offered when creating. It opens by itself on the first launch
     * (not after a rotation, when the FragmentManager already restored it if it was open)
     * and can be reopened by the button. The listener is registered on every onCreate, so
     * a choice made right after a rotation is still received.
     */
    private fun setUpCatalog(savedInstanceState: Bundle?) {
        val creating = subscriptionId == null
        binding.catalogButton.isVisible = creating
        if (!creating) return

        supportFragmentManager.setFragmentResultListener(CatalogBottomSheet.REQUEST_KEY, this) { _, result ->
            CatalogBottomSheet.chosenServiceId(result)?.let(viewModel::applyCatalogService)
        }
        binding.catalogButton.setOnClickListener { openCatalog() }
        if (savedInstanceState == null) openCatalog()
    }

    private fun openCatalog() {
        if (supportFragmentManager.findFragmentByTag(CatalogBottomSheet.TAG) != null) return
        CatalogBottomSheet().show(supportFragmentManager, CatalogBottomSheet.TAG)
    }

    private fun openDatePicker() {
        if (findDatePicker() != null) return
        val selected = viewModel.state.value?.startDate ?: LocalDate.now()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.label_start_date)
            .setSelection(selected.toEpochDay() * MILLIS_PER_DAY)
            .setCalendarConstraints(
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()
            )
            .build()
        listenToDatePicker(picker)
        picker.show(supportFragmentManager, DATE_PICKER_TAG)
    }

    @Suppress("UNCHECKED_CAST")
    private fun findDatePicker(): MaterialDatePicker<Long>? =
        supportFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<Long>

    private fun listenToDatePicker(picker: MaterialDatePicker<Long>) {
        picker.addOnPositiveButtonClickListener { millis ->
            viewModel.selectStartDate(LocalDate.ofEpochDay(millis / MILLIS_PER_DAY))
        }
    }

    private fun RuleError.message(): String = getString(messageRes())

    private fun openLogin() {
        startActivity(LoginActivity.newTask(this))
        finish()
    }

    companion object {
        private const val EXTRA_SUBSCRIPTION_ID = "subscription_id"
        private const val NO_ID = -1L
        private const val DATE_PICKER_TAG = "start_date_picker"
        private const val MILLIS_PER_DAY = 86_400_000L

        fun newIntent(context: Context, subscriptionId: Long? = null): Intent =
            Intent(context, SubscriptionFormActivity::class.java).apply {
                subscriptionId?.let { putExtra(EXTRA_SUBSCRIPTION_ID, it) }
            }
    }
}
