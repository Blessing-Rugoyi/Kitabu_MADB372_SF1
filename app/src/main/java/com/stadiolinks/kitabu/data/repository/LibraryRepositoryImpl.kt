package com.stadiolinks.kitabu.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.stadiolinks.kitabu.data.local.KitabuDatabase
import com.stadiolinks.kitabu.data.local.entity.BookingEntity
import com.stadiolinks.kitabu.data.mapper.toDomain
import com.stadiolinks.kitabu.domain.model.Book
import com.stadiolinks.kitabu.domain.model.BookingStatus
import com.stadiolinks.kitabu.domain.model.LibraryError
import com.stadiolinks.kitabu.domain.model.LibraryResult
import com.stadiolinks.kitabu.domain.model.RentalDuration
import com.stadiolinks.kitabu.domain.model.Reservation
import com.stadiolinks.kitabu.domain.policy.RentalPolicy
import com.stadiolinks.kitabu.domain.repository.LibraryRepository
import com.stadiolinks.kitabu.domain.util.InputSanitizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation. Every multi-step change runs inside a single database
 * transaction, and all writes are suspend functions so callers can run them off the
 * main thread (Room dispatches them to its own executors).
 */
class LibraryRepositoryImpl(
    private val database: KitabuDatabase,
    private val policy: RentalPolicy = RentalPolicy()
) : LibraryRepository {

    private val bookDao = database.bookDao()
    private val bookingDao = database.bookingDao()

    override fun observeCatalog(query: String, availability: Boolean?): Flow<List<Book>> {
        val pattern = InputSanitizer.escapeLike(InputSanitizer.sanitizeSearchQuery(query))
        return bookDao.observeCatalog(pattern, availability)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeReservations(): Flow<List<Reservation>> =
        bookingDao.observeOpenBookings().map { rows -> rows.map { it.toDomain() } }

    override suspend fun reserveBook(
        bookId: Long,
        duration: RentalDuration,
        userName: String
    ): LibraryResult<Long> = safeCall<Long> {
        val now = policy.now()
        val deadline = policy.returnDeadlineFor(now, duration)
        if (!policy.isValidDeadline(now, deadline)) {
            return@safeCall LibraryResult.Failure(LibraryError.INVALID_DEADLINE)
        }
        database.withTransaction<LibraryResult<Long>> {
            if (bookDao.getById(bookId) == null) {
                return@withTransaction LibraryResult.Failure(LibraryError.BOOK_NOT_FOUND)
            }
            // Atomic claim: only one caller can flip isAvailable from true to false.
            if (bookDao.claimIfAvailable(bookId) == 0) {
                return@withTransaction LibraryResult.Failure(LibraryError.BOOK_UNAVAILABLE)
            }
            bookingDao.insert(
                BookingEntity(
                    bookOwnerId = bookId,
                    userName = InputSanitizer.sanitizeUserName(userName),
                    bookingDate = now,
                    returnDeadline = deadline,
                    status = BookingStatus.PENDING
                )
            )
            LibraryResult.Success(deadline)
        }
    }

    override suspend fun confirmPickup(bookingId: Long): LibraryResult<Unit> = safeCall<Unit> {
        database.withTransaction<LibraryResult<Unit>> {
            val changed = bookingDao.transitionStatus(bookingId, BookingStatus.ACTIVE, BookingStatus.PENDING)
            if (changed == 0) {
                LibraryResult.Failure(missingOrInvalid(bookingId))
            } else {
                LibraryResult.Success(Unit)
            }
        }
    }

    override suspend fun renewBooking(bookingId: Long): LibraryResult<Long> = safeCall<Long> {
        database.withTransaction<LibraryResult<Long>> {
            val booking = bookingDao.getById(bookingId)
                ?: return@withTransaction LibraryResult.Failure(LibraryError.BOOKING_NOT_FOUND)
            if (booking.status != BookingStatus.ACTIVE) {
                return@withTransaction LibraryResult.Failure(LibraryError.INVALID_STATE)
            }
            if (policy.isOverdue(booking.returnDeadline)) {
                return@withTransaction LibraryResult.Failure(LibraryError.OVERDUE)
            }
            val newDeadline = policy.renewedDeadline(booking.bookingDate, booking.returnDeadline)
                ?: return@withTransaction LibraryResult.Failure(LibraryError.MAX_RENEWALS_REACHED)
            if (!policy.isValidDeadline(booking.bookingDate, newDeadline) || newDeadline <= booking.returnDeadline) {
                return@withTransaction LibraryResult.Failure(LibraryError.INVALID_DEADLINE)
            }
            if (bookingDao.updateReturnDeadline(bookingId, newDeadline) == 0) {
                return@withTransaction LibraryResult.Failure(LibraryError.INVALID_STATE)
            }
            LibraryResult.Success(newDeadline)
        }
    }

    override suspend fun returnBooking(bookingId: Long): LibraryResult<Unit> = safeCall<Unit> {
        database.withTransaction<LibraryResult<Unit>> {
            val booking = bookingDao.getById(bookingId)
                ?: return@withTransaction LibraryResult.Failure(LibraryError.BOOKING_NOT_FOUND)
            val changed = bookingDao.transitionStatus(bookingId, BookingStatus.RETURNED, BookingStatus.ACTIVE)
            if (changed == 0) {
                return@withTransaction LibraryResult.Failure(LibraryError.INVALID_STATE)
            }
            bookDao.setAvailability(booking.bookOwnerId, true)
            LibraryResult.Success(Unit)
        }
    }

    override suspend fun cancelPendingBooking(bookingId: Long): LibraryResult<Unit> = safeCall<Unit> {
        database.withTransaction<LibraryResult<Unit>> {
            val booking = bookingDao.getById(bookingId)
                ?: return@withTransaction LibraryResult.Failure(LibraryError.BOOKING_NOT_FOUND)
            if (bookingDao.deletePending(bookingId) == 0) {
                return@withTransaction LibraryResult.Failure(LibraryError.INVALID_STATE)
            }
            bookDao.setAvailability(booking.bookOwnerId, true)
            LibraryResult.Success(Unit)
        }
    }

    private suspend fun missingOrInvalid(bookingId: Long): LibraryError =
        if (bookingDao.getById(bookingId) == null) {
            LibraryError.BOOKING_NOT_FOUND
        } else {
            LibraryError.INVALID_STATE
        }

    private suspend fun <T> safeCall(block: suspend () -> LibraryResult<T>): LibraryResult<T> =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Database operation failed", e)
            LibraryResult.Failure(LibraryError.STORAGE)
        }

    private companion object {
        const val TAG = "LibraryRepository"
    }
}
