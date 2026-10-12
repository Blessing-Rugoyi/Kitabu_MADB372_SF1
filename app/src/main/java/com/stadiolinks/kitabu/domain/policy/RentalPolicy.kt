package com.stadiolinks.kitabu.domain.policy

import com.stadiolinks.kitabu.domain.model.RentalDuration
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Business rules for rental dates. All dates are stored as epoch milliseconds; day maths
 * is done in the device time zone so "days remaining" matches what the member sees.
 * A [Clock] is injected so the rules can be unit-tested with a fixed date.
 */
class RentalPolicy(private val clock: Clock = Clock.systemDefaultZone()) {

    fun now(): Long = clock.millis()

    fun today(): LocalDate = LocalDate.now(clock)

    /** Return deadline for a rental starting at [startMillis]. */
    fun returnDeadlineFor(startMillis: Long, duration: RentalDuration): Long =
        plusDays(startMillis, duration.days.toLong())

    /** A return date is only valid if it falls after the booking date. */
    fun isValidDeadline(bookingDate: Long, deadline: Long): Boolean = deadline > bookingDate

    /** Whole calendar days until [deadlineMillis] (negative when overdue). */
    fun daysRemaining(deadlineMillis: Long, today: LocalDate = today()): Long =
        ChronoUnit.DAYS.between(today, Instant.ofEpochMilli(deadlineMillis).atZone(clock.zone).toLocalDate())

    fun isOverdue(deadlineMillis: Long, today: LocalDate = today()): Boolean =
        daysRemaining(deadlineMillis, today) < 0

    /**
     * New deadline after a renewal, or null if it would push the rental past
     * [MAX_TOTAL_RENTAL_DAYS] counted from the booking date.
     */
    fun renewedDeadline(bookingDate: Long, currentDeadline: Long): Long? {
        val candidate = plusDays(currentDeadline, RENEWAL_DAYS)
        val latestAllowed = plusDays(bookingDate, MAX_TOTAL_RENTAL_DAYS)
        return candidate.takeIf { it <= latestAllowed }
    }

    private fun plusDays(epochMillis: Long, days: Long): Long =
        Instant.ofEpochMilli(epochMillis).atZone(clock.zone).plusDays(days).toInstant().toEpochMilli()

    companion object {
        const val RENEWAL_DAYS = 7L
        const val MAX_TOTAL_RENTAL_DAYS = 42L
    }
}
