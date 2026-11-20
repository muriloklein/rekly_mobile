package com.example.rekly.domain.rules

import com.example.rekly.data.entity.Payment
import com.example.rekly.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class PaymentStatusRulesTest {

    private val today = LocalDate.of(2026, 11, 19)

    private fun payment(status: PaymentStatus, paidDate: LocalDate?) = Payment(
        id = 1,
        subscriptionId = 7,
        referenceMonth = YearMonth.of(2026, 11),
        amountCents = 15050,
        dueDate = LocalDate.of(2026, 11, 5),
        paidDate = paidDate,
        status = status
    )

    @Test
    fun markPaid_setsStatusAndTodayAndKeepsTheAmount() {
        val result = PaymentStatusRules.markPaid(payment(PaymentStatus.PENDING, null), today)

        assertEquals(PaymentStatus.PAID, result.status)
        assertEquals(today, result.paidDate)
        assertEquals(15050L, result.amountCents)
    }

    @Test
    fun markPaid_onAPaidPayment_keepsTheOriginalPaidDate() {
        val original = payment(PaymentStatus.PAID, LocalDate.of(2026, 11, 5))

        assertEquals(original, PaymentStatusRules.markPaid(original, today))
    }

    @Test
    fun markUnpaid_returnsToPendingWithoutPaidDateAndKeepsTheAmount() {
        val result = PaymentStatusRules.markUnpaid(payment(PaymentStatus.PAID, LocalDate.of(2026, 11, 5)))

        assertEquals(PaymentStatus.PENDING, result.status)
        assertNull(result.paidDate)
        assertEquals(15050L, result.amountCents)
    }
}
