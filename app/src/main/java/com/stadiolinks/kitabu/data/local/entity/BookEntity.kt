package com.stadiolinks.kitabu.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = BookEntity.TABLE_NAME)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val bookId: Long = 0,
    val title: String,
    val author: String,
    val category: String,
    @ColumnInfo(defaultValue = "1") val isAvailable: Boolean = true
) {
    companion object {
        const val TABLE_NAME = "books"
    }
}
