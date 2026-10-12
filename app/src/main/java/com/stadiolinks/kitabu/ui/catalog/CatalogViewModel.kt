package com.stadiolinks.kitabu.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stadiolinks.kitabu.R
import com.stadiolinks.kitabu.domain.model.Book
import com.stadiolinks.kitabu.domain.model.LibraryResult
import com.stadiolinks.kitabu.domain.model.Member
import com.stadiolinks.kitabu.domain.model.RentalDuration
import com.stadiolinks.kitabu.domain.policy.RentalPolicy
import com.stadiolinks.kitabu.domain.repository.LibraryRepository
import com.stadiolinks.kitabu.domain.util.InputSanitizer
import com.stadiolinks.kitabu.ui.common.UserMessage
import com.stadiolinks.kitabu.ui.common.toUserMessage
import com.stadiolinks.kitabu.ui.util.formatDisplayDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds catalogue state. The book list comes straight from a Room Flow, so a reservation
 * or return made anywhere in the app updates the availability badges immediately.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(
    private val repository: LibraryRepository,
    private val policy: RentalPolicy
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(CatalogFilter.ALL)
    private val reservationSheet = MutableStateFlow<ReservationSheetState?>(null)
    private val messageChannel = Channel<UserMessage>(Channel.BUFFERED)

    /** One-off messages (snackbar). */
    val messages: Flow<UserMessage> = messageChannel.receiveAsFlow()

    private val books: Flow<List<Book>> = combine(query, filter) { q, f -> q to f }
        .distinctUntilChanged()
        .flatMapLatest { (q, f) -> repository.observeCatalog(q, f.availability) }

    val uiState: StateFlow<CatalogUiState> =
        combine(query, filter, books, reservationSheet) { q, f, list, sheet ->
            CatalogUiState(
                query = q,
                filter = f,
                books = list,
                isLoading = false,
                reservationSheet = sheet
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CatalogUiState()
        )

    fun onQueryChange(value: String) {
        query.value = InputSanitizer.limitSearchInput(value)
    }

    fun onClearQuery() {
        query.value = ""
    }

    fun onFilterSelected(selected: CatalogFilter) {
        filter.value = selected
    }

    fun onBookClick(book: Book) {
        if (!book.isAvailable) {
            sendMessage(UserMessage(R.string.msg_book_borrowed, listOf(book.title)))
            return
        }
        reservationSheet.value = ReservationSheetState(
            book = book,
            duration = RentalDuration.DEFAULT,
            returnDate = policy.returnDeadlineFor(policy.now(), RentalDuration.DEFAULT)
        )
    }

    fun onDurationSelected(duration: RentalDuration) {
        reservationSheet.update { current ->
            current?.copy(
                duration = duration,
                returnDate = policy.returnDeadlineFor(policy.now(), duration)
            )
        }
    }

    fun onSheetDismissed() {
        reservationSheet.value = null
    }

    fun onReserveConfirmed() {
        val sheet = reservationSheet.value ?: return
        if (sheet.isSubmitting) return
        reservationSheet.value = sheet.copy(isSubmitting = true)

        viewModelScope.launch {
            val result = repository.reserveBook(sheet.book.id, sheet.duration, Member.DEFAULT_NAME)
            reservationSheet.value = null
            val message = when (result) {
                is LibraryResult.Success -> UserMessage(
                    R.string.msg_reserved,
                    listOf(sheet.book.title, formatDisplayDate(result.value))
                )
                is LibraryResult.Failure -> result.error.toUserMessage()
            }
            messageChannel.send(message)
        }
    }

    private fun sendMessage(message: UserMessage) {
        viewModelScope.launch { messageChannel.send(message) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
