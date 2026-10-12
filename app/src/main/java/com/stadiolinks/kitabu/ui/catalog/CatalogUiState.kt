package com.stadiolinks.kitabu.ui.catalog

import androidx.annotation.StringRes
import com.stadiolinks.kitabu.R
import com.stadiolinks.kitabu.domain.model.Book
import com.stadiolinks.kitabu.domain.model.RentalDuration

/** Availability filter chips. [availability] is passed straight to the database query. */
enum class CatalogFilter(@StringRes val labelRes: Int, val availability: Boolean?) {
    ALL(R.string.filter_all, null),
    AVAILABLE(R.string.filter_available, true),
    BORROWED(R.string.filter_borrowed, false)
}

/** State of the reservation bottom sheet while it is open. */
data class ReservationSheetState(
    val book: Book,
    val duration: RentalDuration = RentalDuration.DEFAULT,
    /** Calculated return date (epoch millis) for the selected [duration]. */
    val returnDate: Long,
    val isSubmitting: Boolean = false
)

data class CatalogUiState(
    val query: String = "",
    val filter: CatalogFilter = CatalogFilter.ALL,
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val reservationSheet: ReservationSheetState? = null
)
