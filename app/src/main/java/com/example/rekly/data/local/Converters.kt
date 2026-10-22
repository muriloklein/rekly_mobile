package com.example.rekly.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun deLocalDate(data: LocalDate?): Long? = data?.toEpochDay()

    @TypeConverter
    fun paraLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }
}
