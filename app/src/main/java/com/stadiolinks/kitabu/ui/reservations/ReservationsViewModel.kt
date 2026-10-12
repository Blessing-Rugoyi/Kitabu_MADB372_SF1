package com.stadiolinks.kitabu.ui.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stadiolinks.kitabu.R
import com.stadiolinks.kitabu.domain.model.LibraryResult
import com.stadiolinks.kitabu.domain.model.Reservation
import com.stadiolinks.kitabu.domain.policy.RentalPolicy
import com.stadiolinks.kitabu.domain.repository.LibraryRepository
import com.stadiolinks.kitabu.ui.common.UserMessage
import com.stadiolinks.kitabu.ui.common.toUserMessage
import com.stadiolinks.kitabu.ui.util.formatDisplayDate
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the "My reservations" dashboard. The list is a Room Flow, so renew / return /
 * cancel results appear on screen without any manual refresh.
 */
class ReservationsViewModel(
    private val repository: LibraryRepository,
    private val policy: RentalPolicy
) : ViewModel() {

    private val busyIds = MutableStateFlow<Set<Long>>(emptySet())
    private val messageChannel = Channel<UserMessage>(Channel.BUFFERED)

    val messages: Flow<UserMessage> = messageChannel.receiveAsFlow()

    /** Emits today's date and again whenever the day changes, so "days left" stays correct. */
    private val today: Flow<LocalDate> = flow {
        while (true) {
            emit(policy.today())
            delay(TimeUnit.MINUTES.toMillis(1))
        }
    }.distinctUntilChanged()

    val uiState: StateFlow<ReservationsUiState> =
        combine(repository.observeReservations(), busyIds, today) { reservations, busy, todayDate ->
            ReservationsUiState(
                items = reservations.map { reservation ->
                    val days = policy.daysRemaining(reservation.returnDeadline, todayDate)
                    ReservationItemUi(
                        reservation = reservation,
                        daysRemaining = days,
                        isOverdue = days < 0,
                        isBusy = reservation.bookingId in busy
                    )
                },
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ReservationsUiState()
        )

    fun onConfirmPickup(reservation: Reservation) = runAction(reservation.bookingId) {
        when (val result = repository.confirmPickup(reservation.bookingId)) {
            is LibraryResult.Success ->
                UserMessage(R.string.msg_pickup_confirmed, listOf(reservation.title))
            is LibraryResult.Failure -> result.error.toUserMessage()
        }
    }

    fun onRenew(reservation: Reservation) = runAction(reservation.bookingId) {
        when (val result = repository.renewBooking(reservation.bookingId)) {
            is LibraryResult.Success -> UserMessage(
                R.string.msg_renewed,
                listOf(reservation.title, formatDisplayDate(result.value))
            )
            is LibraryResult.Failure -> result.error.toUserMessage()
        }
    }

    fun onReturn(reservation: Reservation) = runAction(reservation.bookingId) {
        when (val result = repository.returnBooking(reservation.bookingId)) {
            is LibraryResult.Success -> UserMessage(R.string.msg_returned, listOf(reservation.title))
            is LibraryResult.Failure -> result.error.toUserMessage()
        }
    }

    fun onCancel(reservation: Reservation) = runAction(reservation.bookingId) {
        when (val result = repository.cancelPendingBooking(reservation.bookingId)) {
            is LibraryResult.Success -> UserMessage(R.string.msg_cancelled, listOf(reservation.title))
            is LibraryResult.Failure -> result.error.toUserMessage()
        }
    }

    /** Runs one action per booking at a time and reports its outcome as a snackbar message. */
    private fun runAction(bookingId: Long, action: suspend () -> UserMessage) {
        if (bookingId in busyIds.value) return
        busyIds.update { it + bookingId }
        viewModelScope.launch {
            try {
                messageChannel.send(action())
            } finally {
                busyIds.update { it - bookingId }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
