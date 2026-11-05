package com.example.rekly.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateFormatter {

    private val dayMonthYear = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun format(date: LocalDate): String = date.format(dayMonthYear)
}
