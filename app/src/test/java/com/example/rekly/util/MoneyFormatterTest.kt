package com.example.rekly.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun parses_amount_with_comma() {
        assertEquals(3990L, MoneyFormatter.parseCents("39,90"))
    }

    @Test
    fun parses_amount_with_dot() {
        assertEquals(3990L, MoneyFormatter.parseCents("39.90"))
    }

    @Test
    fun parses_integer_amount() {
        assertEquals(3900L, MoneyFormatter.parseCents("39"))
    }

    @Test
    fun parses_amount_with_one_decimal_digit() {
        assertEquals(3950L, MoneyFormatter.parseCents("39,5"))
    }

    @Test
    fun parses_amount_with_thousands_separator() {
        assertEquals(123456L, MoneyFormatter.parseCents("1.234,56"))
    }

    @Test
    fun ignores_currency_prefix_and_spaces() {
        assertEquals(3990L, MoneyFormatter.parseCents(" R$ 39,90 "))
    }

    @Test
    fun rejects_invalid_text() {
        assertNull(MoneyFormatter.parseCents(""))
        assertNull(MoneyFormatter.parseCents("abc"))
        assertNull(MoneyFormatter.parseCents("39,999"))
        assertNull(MoneyFormatter.parseCents("-10"))
    }

    @Test
    fun formats_cents_in_reais() {
        assertEquals("R$ 39,90", MoneyFormatter.format(3990))
        assertEquals("R$ 0,05", MoneyFormatter.format(5))
    }

    @Test
    fun formats_thousands_with_dot() {
        assertEquals("R$ 1.234,56", MoneyFormatter.format(123456))
        assertEquals("R$ 1.000.000,00", MoneyFormatter.format(100000000))
    }

    @Test
    fun input_text_goes_back_to_the_same_cents() {
        assertEquals("39,90", MoneyFormatter.toInputText(3990))
        assertEquals(3990L, MoneyFormatter.parseCents(MoneyFormatter.toInputText(3990)))
    }
}
