package com.stadiolinks.kitabu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stadiolinks.kitabu.data.local.entity.BookingEntity
import com.stadiolinks.kitabu.data.local.model.BookingWithBook
import com.stadiolinks.kitabu.domain.model.BookingStatus
import kotlinx.coroutines.flow.Flow

/**
 * Status literals in the SQL below ('PENDING', 'ACTIVE') match how the enum is stored by
 * the type converter (its name).
 */
@Dao
interface BookingDao {

    // ---- Create ----
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(booking: BookingEntity): Long

    // ---- Read ----
    /** Pending and active bookings joined with book details, soonest deadline first. */
    @Query(
        "SELECT bk.bookingId, bk.bookOwnerId, bk.userName, bk.bookingDate, bk.returnDeadline, " +
            "bk.status, b.title, b.author, b.category " +
            "FROM bookings AS bk INNER JOIN books AS b ON b.bookId = bk.bookOwnerId " +
            "WHERE bk.status IN ('PENDING', 'ACTIVE') " +
            "ORDER BY bk.returnDeadline ASC"
    )
    fun observeOpenBookings(): Flow<List<BookingWithBook>>

    @Query("SELECT * FROM bookings WHERE bookingId = :bookingId")
    suspend fun getById(bookingId: Long): BookingEntity?

    // ---- Update ----
    /** Extends the deadline of an ACTIVE booking. Returns the number of rows changed. */
    @Query("UPDATE bookings SET returnDeadline = :newDeadline WHERE bookingId = :bookingId AND status = 'ACTIVE'")
    suspend fun updateReturnDeadline(bookingId: Long, newDeadline: Long): Int

    /** Moves a booking to [newStatus] only if it is currently in [expectedStatus]. */
    @Query("UPDATE bookings SET status = :newStatus WHERE bookingId = :bookingId AND status = :expectedStatus")
    suspend fun transitionStatus(bookingId: Long, newStatus: BookingStatus, expectedStatus: BookingStatus): Int

    // ---- Delete ----
    /** Deletes a booking only while it is still PENDING (before pickup). */
    @Query("DELETE FROM bookings WHERE bookingId = :bookingId AND status = 'PENDING'")
    suspend fun deletePending(bookingId: Long): Int
}
