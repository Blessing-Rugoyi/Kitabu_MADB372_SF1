package com.stadiolinks.kitabu.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats epoch milliseconds as e.g. "Mon, 12 Oct 2026" in the device time zone and locale. */
fun formatDisplayDate(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
