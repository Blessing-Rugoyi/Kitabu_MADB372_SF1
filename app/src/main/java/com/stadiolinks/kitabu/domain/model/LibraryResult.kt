package com.stadiolinks.kitabu.domain.model

/** Reasons a library action can be refused. Mapped to user-facing text in the UI layer. */
enum class LibraryError {
    BOOK_NOT_FOUND,
    BOOK_UNAVAILABLE,
    BOOKING_NOT_FOUND,
    INVALID_STATE,
    OVERDUE,
    MAX_RENEWALS_REACHED,
    INVALID_DEADLINE,
    STORAGE
}

/** Outcome of a repository action: either a value or a [LibraryError]. */
sealed interface LibraryResult<out T> {
    data class Success<out T>(val value: T) : LibraryResult<T>
    data class Failure(val error: LibraryError) : LibraryResult<Nothing>
}
