package com.stadiolinks.kitabu.domain

import com.stadiolinks.kitabu.domain.model.RentalDuration
import com.stadiolinks.kitabu.domain.policy.RentalPolicy
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RentalPolicyTest {

    private val start = Instant.parse("2026-09-29T10:00:00Z")
    private val clock = Clock.fixed(start, ZoneOffset.UTC)
    private val policy = RentalPolicy(clock)

    private fun millis(iso: String): Long = Instant.parse(iso).toEpochMilli()

    @Test
    fun returnDeadline_addsSelectedNumberOfDays() {
        assertEquals(millis("2026-10-06T10:00:00Z"), policy.returnDeadlineFor(start.toEpochMilli(), RentalDuration.ONE_WEEK))
        assertEquals(millis("2026-10-13T10:00:00Z"), policy.returnDeadlineFor(start.toEpochMilli(), RentalDuration.TWO_WEEKS))
        assertEquals(millis("2026-10-20T10:00:00Z"), policy.returnDeadlineFor(start.toEpochMilli(), RentalDuration.THREE_WEEKS))
    }

    @Test
    fun daysRemaining_countsCalendarDays() {
        val today = LocalDate.of(2026, 9, 29)
        assertEquals(14, policy.daysRemaining(millis("2026-10-13T10:00:00Z"), today))
        assertEquals(0, policy.daysRemaining(millis("2026-09-29T23:00:00Z"), today))
        assertEquals(-2, policy.daysRemaining(millis("2026-09-27T10:00:00Z"), today))
    }

    @Test
    fun isOverdue_onlyWhenDeadlineDayHasPassed() {
        val today = LocalDate.of(2026, 9, 29)
        assertFalse(policy.isOverdue(millis("2026-09-29T08:00:00Z"), today))
        assertTrue(policy.isOverdue(millis("2026-09-28T23:59:00Z"), today))
    }

    @Test
    fun isValidDeadline_requiresDeadlineAfterBookingDate() {
        assertTrue(policy.isValidDeadline(1_000L, 2_000L))
        assertFalse(policy.isValidDeadline(2_000L, 2_000L))
        assertFalse(policy.isValidDeadline(2_000L, 1_000L))
    }

    @Test
    fun renewedDeadline_extendsByRenewalPeriod() {
        val booked = millis("2026-09-29T10:00:00Z")
        val deadline = millis("2026-10-13T10:00:00Z")
        assertEquals(millis("2026-10-20T10:00:00Z"), policy.renewedDeadline(booked, deadline))
    }

    @Test
    fun renewedDeadline_stopsAtMaximumTotalRental() {
        val booked = millis("2026-09-29T10:00:00Z")
        // 42 days after booking is the cap, so a deadline already at the cap cannot be renewed.
        val atCap = millis("2026-11-10T10:00:00Z")
        assertNull(policy.renewedDeadline(booked, atCap))
        // One week short of the cap can still be renewed.
        assertNotNull(policy.renewedDeadline(booked, millis("2026-11-03T10:00:00Z")))
    }
}
