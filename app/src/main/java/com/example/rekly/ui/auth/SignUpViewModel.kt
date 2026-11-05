package com.example.rekly.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.repository.UserRepository
import com.example.rekly.domain.model.OperationResult
import com.example.rekly.domain.model.RuleError
import com.example.rekly.domain.rules.Validators
import kotlinx.coroutines.launch

data class SignUpState(
    val nameError: RuleError? = null,
    val emailError: RuleError? = null,
    val passwordError: RuleError? = null,
    val confirmationError: RuleError? = null,
    val loading: Boolean = false,
    val registeredUserId: Long? = null
) {
    fun hasErrors(): Boolean =
        nameError != null || emailError != null || passwordError != null || confirmationError != null
}

class SignUpViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableLiveData(SignUpState())
    val state: LiveData<SignUpState> = _state

    fun signUp(name: String, email: String, password: String, confirmation: String) {
        val validation = SignUpState(
            nameError = Validators.validateName(name),
            emailError = Validators.validateEmail(email),
            passwordError = Validators.validateNewPassword(password),
            confirmationError = Validators.validateConfirmation(password, confirmation)
        )
        if (validation.hasErrors()) {
            _state.value = validation
            return
        }

        _state.value = SignUpState(loading = true)
        viewModelScope.launch {
            _state.value = when (val result = userRepository.register(name, email, password)) {
                is OperationResult.Success -> SignUpState(registeredUserId = result.value)
                is OperationResult.Failure -> SignUpState(emailError = result.error)
            }
        }
    }

    companion object {
        fun factory(userRepository: UserRepository) = viewModelFactory {
            initializer { SignUpViewModel(userRepository) }
        }
    }
}
