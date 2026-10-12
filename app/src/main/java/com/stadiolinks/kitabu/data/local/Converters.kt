package com.stadiolinks.kitabu.data.local

import androidx.room.TypeConverter
import com.stadiolinks.kitabu.domain.model.BookingStatus

/** Maps types SQLite cannot store natively to primitives. Dates are stored as Long epoch millis. */
class Converters {

    @TypeConverter
    fun fromBookingStatus(status: BookingStatus): String = status.name

    @TypeConverter
    fun toBookingStatus(value: String): BookingStatus = BookingStatus.valueOf(value)
}
