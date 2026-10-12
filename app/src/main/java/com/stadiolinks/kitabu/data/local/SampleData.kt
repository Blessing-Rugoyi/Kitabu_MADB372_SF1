package com.stadiolinks.kitabu.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stadiolinks.kitabu.data.local.entity.BookEntity
import com.stadiolinks.kitabu.data.local.entity.BookingEntity
import com.stadiolinks.kitabu.domain.model.BookingStatus
import com.stadiolinks.kitabu.domain.model.Member
import java.util.concurrent.TimeUnit

/** Sample records inserted once, when the database file is first created. */
internal object SampleData {

    private data class SeedBook(val title: String, val author: String, val category: String)

    private data class SeedBooking(
        val bookIndex: Int,
        val status: BookingStatus,
        val reservedDaysAgo: Long,
        val dueInDays: Long
    )

    private val books = listOf(
        SeedBook("Clean Architecture", "Robert C. Martin", "Software Engineering"),
        SeedBook("Kotlin in Action", "Dmitry Jemerov & Svetlana Isakova", "Programming"),
        SeedBook("Designing Data-Intensive Applications", "Martin Kleppmann", "Databases"),
        SeedBook("Don't Make Me Think", "Steve Krug", "Web Design"),
        SeedBook("Introduction to Algorithms", "Cormen, Leiserson, Rivest & Stein", "Algorithms"),
        SeedBook("Research Design", "John W. Creswell", "Research Methods"),
        SeedBook("Database System Concepts", "Silberschatz, Korth & Sudarshan", "Databases"),
        SeedBook("The Design of Everyday Things", "Don Norman", "Design"),
        SeedBook("Information Systems Today", "Joseph Valacich & Christoph Schneider", "Information Systems"),
        SeedBook("Refactoring", "Martin Fowler", "Software Engineering"),
        SeedBook("Human-Computer Interaction", "Alan Dix, Janet Finlay, Gregory Abowd & Russell Beale", "Design"),
        SeedBook("Things Fall Apart", "Chinua Achebe", "Literature")
    )

    /** Books that start out borrowed, each with a matching booking so the data stays consistent. */
    private val bookings = listOf(
        SeedBooking(bookIndex = 1, status = BookingStatus.ACTIVE, reservedDaysAgo = 5, dueInDays = 9),
        SeedBooking(bookIndex = 4, status = BookingStatus.ACTIVE, reservedDaysAgo = 12, dueInDays = 2),
        SeedBooking(bookIndex = 7, status = BookingStatus.PENDING, reservedDaysAgo = 1, dueInDays = 13)
    )

    fun seed(db: SupportSQLiteDatabase, nowMillis: Long = System.currentTimeMillis()) {
        val borrowedIndexes = bookings.map { it.bookIndex }.toSet()

        val bookIds = books.mapIndexed { index, book ->
            val values = ContentValues().apply {
                put("title", book.title)
                put("author", book.author)
                put("category", book.category)
                put("isAvailable", if (index in borrowedIndexes) 0 else 1)
            }
            db.insert(BookEntity.TABLE_NAME, SQLiteDatabase.CONFLICT_ABORT, values)
        }

        bookings.forEach { booking ->
            val values = ContentValues().apply {
                put("bookOwnerId", bookIds[booking.bookIndex])
                put("userName", Member.DEFAULT_NAME)
                put("bookingDate", nowMillis - TimeUnit.DAYS.toMillis(booking.reservedDaysAgo))
                put("returnDeadline", nowMillis + TimeUnit.DAYS.toMillis(booking.dueInDays))
                put("status", booking.status.name)
            }
            db.insert(BookingEntity.TABLE_NAME, SQLiteDatabase.CONFLICT_ABORT, values)
        }
    }
}
