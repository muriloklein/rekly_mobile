package com.example.rekly.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DateFormatter {

    private val dayMonthYear = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val monthYear = DateTimeFormatter.ofPattern("MM/yyyy")

    fun format(date: LocalDate): String = date.format(dayMonthYear)

    fun formatMonth(month: YearMonth): String = month.format(monthYear)
}
