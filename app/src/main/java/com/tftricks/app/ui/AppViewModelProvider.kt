package com.tftricks.app.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tftricks.app.TFTricksApplication
import com.tftricks.app.ui.screens.augments.AugmentsViewModel
import com.tftricks.app.ui.screens.champions.ChampionsViewModel
import com.tftricks.app.ui.screens.comps.TeamCompsViewModel
import com.tftricks.app.ui.screens.home.HomeViewModel
import com.tftricks.app.ui.screens.items.ItemsViewModel
import com.tftricks.app.ui.screens.patchnotes.PatchNotesViewModel
import com.tftricks.app.ui.screens.traits.TraitsViewModel

/**
 * Factory for every ViewModel in the app, wired to the repositories in
 * [TFTricksApplication]'s AppContainer.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { HomeViewModel(app().container) }
        initializer { TeamCompsViewModel(app().container.teamCompRepository) }
        initializer { ChampionsViewModel(app().container.championRepository) }
        initializer { TraitsViewModel(app().container.traitRepository) }
        initializer { ItemsViewModel(app().container.itemRepository) }
        initializer { AugmentsViewModel(app().container.augmentRepository) }
        initializer { PatchNotesViewModel(app().container.patchNoteRepository) }
    }

    private fun CreationExtras.app(): TFTricksApplication =
        this[APPLICATION_KEY] as TFTricksApplication
}
