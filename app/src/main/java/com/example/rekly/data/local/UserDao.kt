package com.example.rekly.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rekly.data.entity.User

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Long): LiveData<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("UPDATE users SET onboardingCompleted = 1 WHERE id = :userId")
    suspend fun markOnboardingCompleted(userId: Long)
}
