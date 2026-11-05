package com.example.rekly.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.rekly.domain.model.PaymentStatus
import java.time.LocalDate
import java.time.YearMonth

/**
 * Payment of a subscription for a reference month.
 *
 * The unique index on subscription + reference month prevents generating the same month twice.
 * The foreign key with RESTRICT prevents deleting a subscription that has payments.
 */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Subscription::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["subscriptionId", "referenceMonth"], unique = true)]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subscriptionId: Long,
    val referenceMonth: YearMonth,
    /** Amount paid or, if pending, an estimate (last amount paid). */
    val amountCents: Long,
    val dueDate: LocalDate,
    /** Null while pending. */
    val paidDate: LocalDate? = null,
    val status: PaymentStatus
)
