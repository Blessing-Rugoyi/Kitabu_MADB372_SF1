package com.stadiolinks.kitabu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stadiolinks.kitabu.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    // ---- Create ----
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(books: List<BookEntity>): List<Long>

    // ---- Read (all return Flow so the UI updates automatically) ----
    @Query("SELECT * FROM books ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<BookEntity>>

    /** Books filtered by availability. */
    @Query("SELECT * FROM books WHERE isAvailable = :isAvailable ORDER BY title COLLATE NOCASE ASC")
    fun observeByAvailability(isAvailable: Boolean): Flow<List<BookEntity>>

    /** LIKE search on title or author. [escapedQuery] must already have LIKE wildcards escaped. */
    @Query(
        "SELECT * FROM books " +
            "WHERE title LIKE '%' || :escapedQuery || '%' ESCAPE '\\' " +
            "OR author LIKE '%' || :escapedQuery || '%' ESCAPE '\\' " +
            "ORDER BY title COLLATE NOCASE ASC"
    )
    fun search(escapedQuery: String): Flow<List<BookEntity>>

    /** Search and availability filter combined; a null [availability] means "all books". */
    @Query(
        "SELECT * FROM books " +
            "WHERE (title LIKE '%' || :escapedQuery || '%' ESCAPE '\\' " +
            "OR author LIKE '%' || :escapedQuery || '%' ESCAPE '\\') " +
            "AND (:availability IS NULL OR isAvailable = :availability) " +
            "ORDER BY title COLLATE NOCASE ASC"
    )
    fun observeCatalog(escapedQuery: String, availability: Boolean?): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE bookId = :bookId")
    suspend fun getById(bookId: Long): BookEntity?

    // ---- Update ----
    @Query("UPDATE books SET isAvailable = :isAvailable WHERE bookId = :bookId")
    suspend fun setAvailability(bookId: Long, isAvailable: Boolean): Int

    /** Atomically claims a book; returns 1 if it was available, 0 if someone else got it. */
    @Query("UPDATE books SET isAvailable = 0 WHERE bookId = :bookId AND isAvailable = 1")
    suspend fun claimIfAvailable(bookId: Long): Int
}
