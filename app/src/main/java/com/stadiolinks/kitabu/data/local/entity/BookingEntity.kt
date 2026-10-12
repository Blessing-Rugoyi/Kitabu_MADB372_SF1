package com.stadiolinks.kitabu.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.stadiolinks.kitabu.domain.model.BookingStatus

@Entity(
    tableName = BookingEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookOwnerId"]), Index(value = ["status"])]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val bookingId: Long = 0,
    val bookOwnerId: Long,
    val userName: String,
    /** Epoch milliseconds. */
    val bookingDate: Long,
    /** Epoch milliseconds. */
    val returnDeadline: Long,
    val status: BookingStatus
) {
    companion object {
        const val TABLE_NAME = "bookings"
    }
}
