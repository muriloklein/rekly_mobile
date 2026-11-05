package com.example.rekly.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rekly.data.entity.Payment
import com.example.rekly.domain.model.PaymentStatus
import java.time.YearMonth

@Dao
interface PaymentDao {

    /**
     * Inserts ignoring reference months that already exist (unique index), which makes
     * payment generation safe even if it runs twice at the same time.
     * Returns -1 for each ignored row.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(payments: List<Payment>): List<Long>

    @Update
    suspend fun update(payment: Payment)

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun findById(id: Long): Payment?

    @Query("SELECT referenceMonth FROM payments WHERE subscriptionId = :subscriptionId")
    suspend fun listReferenceMonths(subscriptionId: Long): List<YearMonth>

    @Query(
        "SELECT amountCents FROM payments WHERE subscriptionId = :subscriptionId AND status = :status " +
            "ORDER BY referenceMonth DESC LIMIT 1"
    )
    suspend fun lastAmount(subscriptionId: Long, status: PaymentStatus): Long?

    @Query("SELECT COUNT(*) FROM payments WHERE subscriptionId = :subscriptionId")
    suspend fun countBySubscription(subscriptionId: Long): Int
}
