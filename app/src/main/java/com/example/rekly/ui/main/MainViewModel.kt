package com.example.rekly.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.repository.PaymentRepository
import kotlinx.coroutines.launch

class MainViewModel(private val paymentRepository: PaymentRepository) : ViewModel() {

    fun generatePayments(userId: Long) {
        viewModelScope.launch { paymentRepository.generatePresumed(userId) }
    }

    companion object {
        fun factory(paymentRepository: PaymentRepository) = viewModelFactory {
            initializer { MainViewModel(paymentRepository) }
        }
    }
}
