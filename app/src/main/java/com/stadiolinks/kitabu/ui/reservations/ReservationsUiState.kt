package com.stadiolinks.kitabu.ui.reservations

import com.stadiolinks.kitabu.domain.model.Reservation

/** A dashboard row: the reservation plus values derived from today's date. */
data class ReservationItemUi(
    val reservation: Reservation,
    /** Whole calendar days until the deadline; negative when overdue. */
    val daysRemaining: Long,
    val isOverdue: Boolean,
    /** True while an action on this row is in flight, so buttons cannot be double-tapped. */
    val isBusy: Boolean
)

data class ReservationsUiState(
    val items: List<ReservationItemUi> = emptyList(),
    val isLoading: Boolean = true
)
