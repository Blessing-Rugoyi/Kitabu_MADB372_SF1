package com.stadiolinks.kitabu.di

import android.content.Context
import com.stadiolinks.kitabu.data.local.KitabuDatabase
import com.stadiolinks.kitabu.data.repository.LibraryRepositoryImpl
import com.stadiolinks.kitabu.domain.policy.RentalPolicy
import com.stadiolinks.kitabu.domain.repository.LibraryRepository

/** Manual dependency container (keeps the project free of a DI framework). */
interface AppContainer {
    val rentalPolicy: RentalPolicy
    val libraryRepository: LibraryRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

    // Created lazily; constructing the database object does not touch the disk.
    private val database: KitabuDatabase by lazy { KitabuDatabase.getInstance(appContext) }

    override val rentalPolicy: RentalPolicy = RentalPolicy()

    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(database, rentalPolicy)
    }
}
