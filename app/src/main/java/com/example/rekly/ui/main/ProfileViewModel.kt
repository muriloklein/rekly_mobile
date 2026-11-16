package com.example.rekly.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rekly.data.entity.User
import com.example.rekly.data.repository.UserRepository

class ProfileViewModel(userRepository: UserRepository, userId: Long) : ViewModel() {

    val user: LiveData<User?> = userRepository.observe(userId)

    companion object {
        fun factory(userRepository: UserRepository, userId: Long) = viewModelFactory {
            initializer { ProfileViewModel(userRepository, userId) }
        }
    }
}
