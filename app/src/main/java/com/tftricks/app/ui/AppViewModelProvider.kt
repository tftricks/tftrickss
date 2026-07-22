package com.tftricks.app.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tftricks.app.TFTricksApplication
import com.tftricks.app.ui.screens.augments.AugmentsViewModel
import com.tftricks.app.ui.screens.builder.TeamBuilderViewModel
import com.tftricks.app.ui.screens.champions.ChampionDetailViewModel
import com.tftricks.app.ui.screens.champions.ChampionsViewModel
import com.tftricks.app.ui.screens.comps.CompDetailViewModel
import com.tftricks.app.ui.screens.comps.TeamCompsViewModel
import com.tftricks.app.ui.screens.home.HomeViewModel
import com.tftricks.app.ui.screens.items.ItemDetailViewModel
import com.tftricks.app.ui.screens.items.ItemsViewModel
import com.tftricks.app.ui.screens.patchnotes.PatchNoteDetailViewModel
import com.tftricks.app.ui.screens.patchnotes.PatchNotesViewModel
import com.tftricks.app.ui.screens.saved.SavedCompsViewModel
import com.tftricks.app.ui.screens.search.SearchViewModel
import com.tftricks.app.ui.screens.settings.OverlaySettingsViewModel
import com.tftricks.app.ui.screens.settings.SettingsViewModel
import com.tftricks.app.ui.screens.traits.TraitsViewModel

/**
 * Factory for every ViewModel in the app, wired to the repositories in
 * [TFTricksApplication]'s AppContainer.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { HomeViewModel(app().container) }
        initializer {
            TeamCompsViewModel(
                app().container.teamCompRepository,
                app().container.championRepository,
                app().container.itemRepository,
                app().container.favoritesRepository
            )
        }
        initializer { CompDetailViewModel(createSavedStateHandle(), app().container) }
        initializer {
            ChampionsViewModel(
                app().container.championRepository,
                app().container.favoritesRepository
            )
        }
        initializer { ChampionDetailViewModel(createSavedStateHandle(), app().container) }
        initializer {
            TraitsViewModel(
                app().container.traitRepository,
                app().container.championRepository
            )
        }
        initializer {
            ItemsViewModel(
                app().container.itemRepository,
                app().container.favoritesRepository
            )
        }
        initializer { ItemDetailViewModel(createSavedStateHandle(), app().container) }
        initializer {
            AugmentsViewModel(
                app().container.augmentRepository,
                app().container.teamCompRepository,
                app().container.favoritesRepository
            )
        }
        initializer { PatchNotesViewModel(app().container.patchNoteRepository) }
        initializer {
            PatchNoteDetailViewModel(createSavedStateHandle(), app().container.patchNoteRepository)
        }
        initializer { SearchViewModel(app().container) }
        initializer { SavedCompsViewModel(app().container) }
        initializer {
            TeamBuilderViewModel(
                app().container.championRepository,
                app().container.traitRepository,
                app().container.savedTeamsRepository
            )
        }
        initializer { OverlaySettingsViewModel(app()) }
        initializer { SettingsViewModel(app()) }
    }

    private fun CreationExtras.app(): TFTricksApplication =
        this[APPLICATION_KEY] as TFTricksApplication
}
