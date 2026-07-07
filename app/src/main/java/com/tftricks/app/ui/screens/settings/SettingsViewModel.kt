package com.tftricks.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.BuildConfig
import com.tftricks.app.TFTricksApplication
import com.tftricks.app.domain.model.FavoriteCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val app: TFTricksApplication) : ViewModel() {

    val appVersion: String = BuildConfig.VERSION_NAME

    /** Total favorites across all categories, for the "clear favorites" row. */
    val favoritesCount: StateFlow<Int> = combine(
        FavoriteCategory.entries.map { app.container.favoritesRepository.favorites(it) }
    ) { sets -> sets.sumOf { it.size } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun clearAllFavorites() {
        viewModelScope.launch {
            app.container.favoritesRepository.clearAll()
        }
    }
}
