package com.example.rekly.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.YearMonth

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    /** Reference month stored as "YYYY-MM" text, readable in the Database Inspector and sortable. */
    @TypeConverter
    fun fromYearMonth(referenceMonth: YearMonth?): String? = referenceMonth?.toString()

    @TypeConverter
    fun toYearMonth(text: String?): YearMonth? = text?.let { YearMonth.parse(it) }
}
