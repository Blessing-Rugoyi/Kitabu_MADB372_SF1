package com.stadiolinks.kitabu.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stadiolinks.kitabu.KitabuApplication
import com.stadiolinks.kitabu.ui.catalog.CatalogViewModel
import com.stadiolinks.kitabu.ui.reservations.ReservationsViewModel

/** Creates ViewModels with their dependencies taken from the [AppContainer]. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val container = kitabuApplication().container
            CatalogViewModel(container.libraryRepository, container.rentalPolicy)
        }
        initializer {
            val container = kitabuApplication().container
            ReservationsViewModel(container.libraryRepository, container.rentalPolicy)
        }
    }
}

private fun CreationExtras.kitabuApplication(): KitabuApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as KitabuApplication
