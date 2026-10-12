package com.stadiolinks.kitabu.domain.model

/** Rental periods a member can choose from in the reservation sheet. */
enum class RentalDuration(val days: Int) {
    ONE_WEEK(7),
    TWO_WEEKS(14),
    THREE_WEEKS(21);

    companion object {
        val DEFAULT: RentalDuration = TWO_WEEKS
    }
}
