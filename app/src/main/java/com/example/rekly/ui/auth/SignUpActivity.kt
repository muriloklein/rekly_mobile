package com.example.rekly.ui.auth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.repository.UserRepository
import com.example.rekly.databinding.ActivitySignUpBinding
import com.example.rekly.ui.main.MainActivity
import com.example.rekly.util.SessionManager
import com.example.rekly.util.applyFormScreenInsets
import com.example.rekly.util.messageRes

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var sessionManager: SessionManager

    private val viewModel: SignUpViewModel by viewModels {
        SignUpViewModel.factory(UserRepository(AppDatabase.getInstance(this).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyFormScreenInsets(binding.root, binding.toolbar.root)

        binding.signUpButton.setOnClickListener {
            viewModel.signUp(
                name = binding.nameInput.text?.toString().orEmpty(),
                email = binding.emailInput.text?.toString().orEmpty(),
                password = binding.passwordInput.text?.toString().orEmpty(),
                confirmation = binding.confirmationInput.text?.toString().orEmpty()
            )
        }
        viewModel.state.observe(this) { render(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun render(state: SignUpState) {
        binding.nameLayout.error = state.nameError?.let { getString(it.messageRes()) }
        binding.emailLayout.error = state.emailError?.let { getString(it.messageRes()) }
        binding.passwordLayout.error = state.passwordError?.let { getString(it.messageRes()) }
        binding.confirmationLayout.error = state.confirmationError?.let { getString(it.messageRes()) }
        binding.signUpButton.isEnabled = !state.loading

        state.registeredUserId?.let { userId ->
            sessionManager.save(userId)
            openMain()
        }
    }

    private fun openMain() {
        startActivity(MainActivity.newTask(this))
        finish()
    }
}
