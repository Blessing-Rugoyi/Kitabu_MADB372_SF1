package com.stadiolinks.kitabu.data.local.model

import com.stadiolinks.kitabu.domain.model.BookingStatus

/** Flat result of the bookings JOIN books query used by the reservations dashboard. */
data class BookingWithBook(
    val bookingId: Long,
    val bookOwnerId: Long,
    val userName: String,
    val bookingDate: Long,
    val returnDeadline: Long,
    val status: BookingStatus,
    val title: String,
    val author: String,
    val category: String
)
