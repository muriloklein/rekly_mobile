package com.example.rekly.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.YearMonth

class Converters {

    @TypeConverter
    fun deLocalDate(data: LocalDate?): Long? = data?.toEpochDay()

    @TypeConverter
    fun paraLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    /** Competência guardada como texto "AAAA-MM", legível no Database Inspector e ordenável. */
    @TypeConverter
    fun deYearMonth(competencia: YearMonth?): String? = competencia?.toString()

    @TypeConverter
    fun paraYearMonth(texto: String?): YearMonth? = texto?.let { YearMonth.parse(it) }
}
