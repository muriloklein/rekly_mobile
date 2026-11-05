package com.example.rekly.util

object MoneyFormatter {

    private val inputFormat = Regex("^\\d{1,9}([.,]\\d{1,2})?$")

    fun parseCents(text: String): Long? {
        val clean = text.trim().removePrefix("R$").trim()
        // With a comma as the decimal separator, dots are thousands separators ("1.234,56")
        val normalized = if (clean.contains(',')) clean.replace(".", "") else clean
        if (!inputFormat.matches(normalized)) return null

        val parts = normalized.split(',', '.')
        val reais = parts[0].toLong()
        val cents = parts.getOrNull(1)?.padEnd(2, '0')?.toLong() ?: 0L
        return reais * 100 + cents
    }

    fun toInputText(cents: Long): String = "${cents / 100},${twoDigits(cents % 100)}"

    fun format(cents: Long): String = "R$ ${groupThousands(cents / 100)},${twoDigits(cents % 100)}"

    private fun twoDigits(value: Long): String = value.toString().padStart(2, '0')

    private fun groupThousands(value: Long): String =
        value.toString().reversed().chunked(3).joinToString(".").reversed()
}
