package com.example.rekly.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.rekly.R
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.local.DefaultCategories
import com.example.rekly.data.repository.SubscriptionRepository
import com.example.rekly.data.repository.UserRepository
import com.example.rekly.databinding.ActivityOnboardingBinding
import com.example.rekly.databinding.ItemOnboardingChipBinding
import com.example.rekly.databinding.ItemOnboardingGroupBinding
import com.example.rekly.domain.catalog.CatalogService
import com.example.rekly.domain.catalog.ServiceCatalog
import com.example.rekly.ui.auth.LoginActivity
import com.example.rekly.ui.main.MainActivity
import com.example.rekly.util.SessionManager
import com.example.rekly.util.applyFormScreenInsets

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var userId: Long = 0

    private val viewModel: OnboardingViewModel by viewModels {
        val database = AppDatabase.getInstance(this)
        OnboardingViewModel.factory(
            SubscriptionRepository(database.subscriptionDao(), database.paymentDao()),
            UserRepository(database.userDao()),
            userId
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
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.setTitle(R.string.title_onboarding)
        applyFormScreenInsets(binding.root, binding.toolbar.root)

        addServiceGroups(viewModel.state.value?.selectedIds.orEmpty())
        binding.skipButton.setOnClickListener { viewModel.skip() }
        binding.finishButton.setOnClickListener { viewModel.finish() }
        viewModel.state.observe(this) { render(it) }
    }

    override fun onStart() {
        super.onStart()
        if (SessionManager(this).loggedUserId() == null) openLogin()
    }

    private fun addServiceGroups(selectedIds: Set<String>) {
        ServiceCatalog.groupedByCategory().forEach { (categoryId, services) ->
            val group = ItemOnboardingGroupBinding.inflate(layoutInflater, binding.groupsContainer, true)
            group.categoryText.text = DefaultCategories.all[categoryId].orEmpty()
            services.forEach { service -> addChip(group, service, service.id in selectedIds) }
        }
    }

    private fun addChip(group: ItemOnboardingGroupBinding, service: CatalogService, selected: Boolean) {
        val chip = ItemOnboardingChipBinding.inflate(layoutInflater, group.serviceChips, true).root
        chip.text = service.name
        chip.isChecked = selected
        chip.setOnCheckedChangeListener { _, isChecked -> viewModel.toggle(service.id, isChecked) }
    }

    private fun render(state: OnboardingState) {
        val count = state.selectedIds.size
        binding.finishButton.text =
            if (count == 0) getString(R.string.action_finish) else getString(R.string.action_finish_count, count)
        binding.finishButton.isEnabled = !state.saving
        binding.skipButton.isEnabled = !state.saving

        if (state.finished) {
            startActivity(MainActivity.newTask(this, openSubscriptions = true))
            finish()
        }
    }

    private fun openLogin() {
        startActivity(LoginActivity.newTask(this))
        finish()
    }

    companion object {
        fun newTask(context: Context): Intent =
            Intent(context, OnboardingActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
