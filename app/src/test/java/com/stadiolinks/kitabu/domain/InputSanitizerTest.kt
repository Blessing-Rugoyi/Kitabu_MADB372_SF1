package com.stadiolinks.kitabu.domain

import com.stadiolinks.kitabu.domain.model.Member
import com.stadiolinks.kitabu.domain.util.InputSanitizer
import org.junit.Assert.assertEquals
import org.junit.Test

class InputSanitizerTest {

    @Test
    fun sanitizeSearchQuery_trimsAndCollapsesWhitespace() {
        assertEquals("clean code", InputSanitizer.sanitizeSearchQuery("  clean \t  code  "))
    }

    @Test
    fun sanitizeSearchQuery_removesControlCharactersAndCapsLength() {
        assertEquals("abc", InputSanitizer.sanitizeSearchQuery("a\u0000b\u0007c"))
        val long = "x".repeat(InputSanitizer.MAX_SEARCH_LENGTH + 40)
        assertEquals(InputSanitizer.MAX_SEARCH_LENGTH, InputSanitizer.sanitizeSearchQuery(long).length)
    }

    @Test
    fun limitSearchInput_keepsTrailingSpaceWhileTyping() {
        assertEquals("clean ", InputSanitizer.limitSearchInput("clean "))
    }

    @Test
    fun escapeLike_escapesWildcardsAndBackslash() {
        assertEquals("100\\%", InputSanitizer.escapeLike("100%"))
        assertEquals("a\\_b", InputSanitizer.escapeLike("a_b"))
        assertEquals("a\\\\b", InputSanitizer.escapeLike("a\\b"))
    }

    @Test
    fun sanitizeUserName_fallsBackToDefaultWhenBlank() {
        assertEquals(Member.DEFAULT_NAME, InputSanitizer.sanitizeUserName("   \u0000 "))
        assertEquals("Bless R", InputSanitizer.sanitizeUserName("  Bless   R "))
    }
}
