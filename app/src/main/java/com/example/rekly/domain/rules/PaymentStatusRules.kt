package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Payment
import com.example.rekly.domain.model.PaymentStatus
import java.time.LocalDate

/** Changes of a payment's status; the amount is never touched here. */
object PaymentStatusRules {

    /** Paid today. A payment that was already paid keeps its original paid date. */
    fun markPaid(payment: Payment, today: LocalDate): Payment =
        if (payment.status == PaymentStatus.PAID) {
            payment
        } else {
            payment.copy(status = PaymentStatus.PAID, paidDate = today)
        }

    /** Back to pending: there is no paid date while the payment is pending. */
    fun markUnpaid(payment: Payment): Payment =
        payment.copy(status = PaymentStatus.PENDING, paidDate = null)
}
