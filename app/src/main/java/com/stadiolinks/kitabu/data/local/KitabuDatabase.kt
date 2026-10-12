package com.stadiolinks.kitabu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stadiolinks.kitabu.data.local.dao.BookDao
import com.stadiolinks.kitabu.data.local.dao.BookingDao
import com.stadiolinks.kitabu.data.local.entity.BookEntity
import com.stadiolinks.kitabu.data.local.entity.BookingEntity

@Database(
    entities = [BookEntity::class, BookingEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KitabuDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    abstract fun bookingDao(): BookingDao

    companion object {
        private const val DATABASE_NAME = "kitabu.db"

        @Volatile
        private var instance: KitabuDatabase? = null

        /** Thread-safe singleton (double-checked locking). */
        fun getInstance(context: Context): KitabuDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): KitabuDatabase =
            Room.databaseBuilder(context, KitabuDatabase::class.java, DATABASE_NAME)
                .addCallback(PrePopulateCallback)
                // Development only: rebuilds the database when the schema version changes.
                .fallbackToDestructiveMigration()
                .build()
    }

    /**
     * Runs once, on Room's background thread, right after the database file is created.
     * Seeding here (rather than in a coroutine) makes it atomic with database creation.
     */
    private object PrePopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            SampleData.seed(db)
        }
    }
}
