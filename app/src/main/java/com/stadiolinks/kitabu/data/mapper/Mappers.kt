package com.stadiolinks.kitabu.data.mapper

import com.stadiolinks.kitabu.data.local.entity.BookEntity
import com.stadiolinks.kitabu.data.local.model.BookingWithBook
import com.stadiolinks.kitabu.domain.model.Book
import com.stadiolinks.kitabu.domain.model.Reservation

fun BookEntity.toDomain(): Book = Book(
    id = bookId,
    title = title,
    author = author,
    category = category,
    isAvailable = isAvailable
)

fun BookingWithBook.toDomain(): Reservation = Reservation(
    bookingId = bookingId,
    bookId = bookOwnerId,
    title = title,
    author = author,
    category = category,
    userName = userName,
    reservedOn = bookingDate,
    returnDeadline = returnDeadline,
    status = status
)
