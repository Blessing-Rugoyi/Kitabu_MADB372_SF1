package com.stadiolinks.kitabu.domain.model

/** Lifecycle of a booking: reserved (pending pickup) -> active rental -> returned. */
enum class BookingStatus {
    PENDING,
    ACTIVE,
    RETURNED
}
