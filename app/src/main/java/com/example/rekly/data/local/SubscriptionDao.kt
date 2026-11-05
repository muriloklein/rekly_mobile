package com.example.rekly.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.rekly.data.entity.Subscription
import com.example.rekly.data.entity.SubscriptionDetails
import com.example.rekly.data.entity.SubscriptionWithCategory
import com.example.rekly.domain.model.SubscriptionStatus

@Dao
interface SubscriptionDao {

    @Insert
    suspend fun insert(subscription: Subscription): Long

    /** Batch creation from the onboarding. */
    @Insert
    suspend fun insertAll(subscriptions: List<Subscription>): List<Long>

    @Update
    suspend fun update(subscription: Subscription)

    @Delete
    suspend fun delete(subscription: Subscription)

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun findById(id: Long): Subscription?

    @Transaction
    @Query("SELECT * FROM subscriptions WHERE userId = :userId ORDER BY serviceName COLLATE NOCASE")
    fun observeWithCategory(userId: Long): LiveData<List<SubscriptionWithCategory>>

    @Transaction
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun observeDetails(id: Long): LiveData<SubscriptionDetails?>

    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND status = :status")
    suspend fun listByStatus(userId: Long, status: SubscriptionStatus): List<Subscription>

    @Query("SELECT COUNT(*) FROM subscriptions WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int
}
