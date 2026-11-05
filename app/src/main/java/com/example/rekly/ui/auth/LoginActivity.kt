package com.example.rekly.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.rekly.data.local.AppDatabase
import com.example.rekly.data.repository.UserRepository
import com.example.rekly.databinding.ActivityLoginBinding
import com.example.rekly.ui.main.MainActivity
import com.example.rekly.util.SessionManager
import com.example.rekly.util.applyFormScreenInsets
import com.example.rekly.util.messageRes

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(UserRepository(AppDatabase.getInstance(this).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        if (sessionManager.loggedUserId() != null) {
            openMain()
            return
        }

        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)
        applyFormScreenInsets(binding.root, binding.toolbar.root)

        binding.loginButton.setOnClickListener {
            viewModel.login(
                email = binding.emailInput.text?.toString().orEmpty(),
                password = binding.passwordInput.text?.toString().orEmpty()
            )
        }
        binding.goToSignUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        viewModel.state.observe(this) { render(it) }
    }

    private fun render(state: LoginState) {
        binding.emailLayout.error = state.emailError?.let { getString(it.messageRes()) }
        binding.passwordLayout.error = state.passwordError?.let { getString(it.messageRes()) }
        binding.credentialsErrorText.isVisible = state.credentialsError != null
        state.credentialsError?.let { binding.credentialsErrorText.setText(it.messageRes()) }
        binding.loginButton.isEnabled = !state.loading

        state.loggedInUserId?.let { userId ->
            sessionManager.save(userId)
            openMain()
        }
    }

    private fun openMain() {
        startActivity(MainActivity.newTask(this))
        finish()
    }

    companion object {
        fun newTask(context: Context): Intent =
            Intent(context, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
