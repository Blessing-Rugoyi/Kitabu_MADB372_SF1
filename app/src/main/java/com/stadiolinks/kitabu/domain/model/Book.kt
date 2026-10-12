package com.stadiolinks.kitabu.domain.model

/** A title in the library catalogue. */
data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val category: String,
    val isAvailable: Boolean
)
