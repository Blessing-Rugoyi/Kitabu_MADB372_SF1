package com.stadiolinks.kitabu.domain.repository

import com.stadiolinks.kitabu.domain.model.Book
import com.stadiolinks.kitabu.domain.model.LibraryResult
import com.stadiolinks.kitabu.domain.model.RentalDuration
import com.stadiolinks.kitabu.domain.model.Reservation
import kotlinx.coroutines.flow.Flow

/** Single entry point the ViewModels use for all library data and business rules. */
interface LibraryRepository {

    /**
     * Live catalogue filtered by a title/author [query] and, optionally, availability
     * (`null` = all books, `true` = available only, `false` = borrowed only).
     */
    fun observeCatalog(query: String, availability: Boolean?): Flow<List<Book>>

    /** Live list of pending and active reservations joined with their book details. */
    fun observeReservations(): Flow<List<Reservation>>

    /** Reserves an available book; on success returns the calculated return deadline. */
    suspend fun reserveBook(bookId: Long, duration: RentalDuration, userName: String): LibraryResult<Long>

    /** Marks a pending reservation as collected (PENDING -> ACTIVE). */
    suspend fun confirmPickup(bookingId: Long): LibraryResult<Unit>

    /** Extends an active rental; on success returns the new return deadline. */
    suspend fun renewBooking(bookingId: Long): LibraryResult<Long>

    /** Returns an active rental and makes the book available again. */
    suspend fun returnBooking(bookingId: Long): LibraryResult<Unit>

    /** Deletes a pending reservation and makes the book available again. */
    suspend fun cancelPendingBooking(bookingId: Long): LibraryResult<Unit>
}
