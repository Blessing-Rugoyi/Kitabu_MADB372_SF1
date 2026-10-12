package com.stadiolinks.kitabu.domain.model

/** A booking joined with the details of the book it belongs to (dashboard row). */
data class Reservation(
    val bookingId: Long,
    val bookId: Long,
    val title: String,
    val author: String,
    val category: String,
    val userName: String,
    val reservedOn: Long,
    val returnDeadline: Long,
    val status: BookingStatus
)
