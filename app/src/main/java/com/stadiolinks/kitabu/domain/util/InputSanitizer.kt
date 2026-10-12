package com.stadiolinks.kitabu.domain.util

import com.stadiolinks.kitabu.domain.model.Member

/** Cleans free-text input before it is stored or used in a query. */
object InputSanitizer {

    const val MAX_SEARCH_LENGTH = 60
    const val MAX_NAME_LENGTH = 60

    private val whitespace = Regex("\\s+")

    /**
     * Light-touch clean-up used while the user is typing: removes control characters and
     * caps the length, but keeps spaces so typing "Clean " does not lose the space.
     */
    fun limitSearchInput(raw: String): String =
        raw.filterNot { it.isISOControl() }.take(MAX_SEARCH_LENGTH)

    /** Full clean-up used for the actual database query. */
    fun sanitizeSearchQuery(raw: String): String = clean(raw, MAX_SEARCH_LENGTH)

    /** Cleans a member name; falls back to the default member when nothing usable remains. */
    fun sanitizeUserName(raw: String): String =
        clean(raw, MAX_NAME_LENGTH).ifEmpty { Member.DEFAULT_NAME }

    /**
     * Escapes the SQL LIKE wildcards so a user typing "%" or "_" searches for those
     * characters literally. Must be paired with `ESCAPE '\'` in the query.
     */
    fun escapeLike(query: String): String =
        query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private fun clean(raw: String, maxLength: Int): String =
        raw.filterNot { it.isISOControl() }
            .replace(whitespace, " ")
            .trim()
            .take(maxLength)
}
