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

data class LoginState(
    val emailError: RuleError? = null,
    val passwordError: RuleError? = null,
    val credentialsError: RuleError? = null,
    val loading: Boolean = false,
    val loggedInUserId: Long? = null
)

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableLiveData(LoginState())
    val state: LiveData<LoginState> = _state

    fun login(email: String, password: String) {
        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePasswordFilled(password)
        if (emailError != null || passwordError != null) {
            _state.value = LoginState(emailError = emailError, passwordError = passwordError)
            return
        }

        _state.value = LoginState(loading = true)
        viewModelScope.launch {
            _state.value = when (val result = userRepository.authenticate(email, password)) {
                is OperationResult.Success -> LoginState(loggedInUserId = result.value.id)
                is OperationResult.Failure -> LoginState(credentialsError = result.error)
            }
        }
    }

    companion object {
        fun factory(userRepository: UserRepository) = viewModelFactory {
            initializer { LoginViewModel(userRepository) }
        }
    }
}
