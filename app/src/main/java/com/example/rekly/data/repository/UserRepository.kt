package com.example.rekly.data.repository

import com.example.rekly.data.entity.User
import com.example.rekly.data.local.UserDao
import com.example.rekly.domain.model.OperationResult
import com.example.rekly.domain.model.RuleError
import com.example.rekly.domain.rules.PasswordHasher
import java.time.LocalDate

class UserRepository(private val userDao: UserDao) {

    suspend fun register(
        name: String,
        email: String,
        password: String,
        today: LocalDate = LocalDate.now()
    ): OperationResult<Long> {
        val normalizedEmail = normalizeEmail(email)
        if (userDao.findByEmail(normalizedEmail) != null) {
            return OperationResult.Failure(RuleError.EMAIL_ALREADY_REGISTERED)
        }
        val salt = PasswordHasher.generateSalt()
        val id = userDao.insert(
            User(
                name = name.trim(),
                email = normalizedEmail,
                passwordHash = PasswordHasher.hash(password, salt),
                salt = salt,
                createdAt = today
            )
        )
        return OperationResult.Success(id)
    }

    suspend fun authenticate(email: String, password: String): OperationResult<User> {
        val user = userDao.findByEmail(normalizeEmail(email))
        if (user == null || !PasswordHasher.matches(password, user.salt, user.passwordHash)) {
            return OperationResult.Failure(RuleError.INVALID_CREDENTIALS)
        }
        return OperationResult.Success(user)
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase()
}
