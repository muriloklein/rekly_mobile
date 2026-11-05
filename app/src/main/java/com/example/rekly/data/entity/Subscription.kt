package com.example.rekly.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.rekly.domain.model.BillingPeriod
import com.example.rekly.domain.model.SubscriptionStatus
import java.time.LocalDate

/**
 * Recurring subscription or bill.
 *
 * Amounts in cents ([Long]) to avoid floating-point rounding errors.
 * [amountCents], [startDate] and [billingDay] are null in subscriptions created
 * by the onboarding until the user completes them (see [isComplete]).
 */
@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("userId"), Index("categoryId")]
)
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val serviceName: String,
    /** Amount charged each period (not the monthly amount), in cents. */
    val amountCents: Long? = null,
    /** Bills like water and electricity: the generated payment stays pending until the real amount is confirmed. */
    val variableAmount: Boolean = false,
    val period: BillingPeriod = BillingPeriod.MONTHLY,
    val startDate: LocalDate? = null,
    /** Day of the month (1 to 31); in shorter months it falls on the last day. */
    val billingDay: Int? = null,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    /** People who split the expense, for the real cost (minimum 1). */
    val participants: Int = 1,
    /**
     * Date when the subscription last became ACTIVE. Presumed payments are only
     * generated from this date's month onward, so registering an old subscription
     * or converting a trial does not create retroactive payments.
     */
    val activeSince: LocalDate? = null,
    val createdAt: LocalDate
) {
    /** Has the minimum data to enter the calculations and generate payments. */
    fun isComplete(): Boolean = amountCents != null && startDate != null && billingDay != null
}
