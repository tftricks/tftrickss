package com.tftricks.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.domain.model.PanelSize
import com.tftricks.app.domain.repository.OverlayPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreOverlayPrefsRepository(
    private val dataStore: DataStore<Preferences>
) : OverlayPrefsRepository {

    private val opacityKey = floatPreferencesKey("overlay_opacity")
    private val panelSizeKey = stringPreferencesKey("overlay_panel_size")
    private val transparentKey = booleanPreferencesKey("overlay_transparent_bg")
    private val compactKey = booleanPreferencesKey("overlay_compact_mode")
    private val buttonXKey = intPreferencesKey("overlay_button_x")
    private val buttonYKey = intPreferencesKey("overlay_button_y")

    private val defaults = OverlaySettings()

    override val settings: Flow<OverlaySettings> =
        dataStore.data
            // The overlay service blocks on the first emission — fall back to
            // defaults instead of never showing the overlay on a corrupt read.
            .catch { emit(emptyPreferences()) }
            .map { prefs ->
                OverlaySettings(
                    opacity = (prefs[opacityKey] ?: defaults.opacity).coerceIn(0.4f, 1.0f),
                    panelSize = prefs[panelSizeKey]
                        ?.let { raw -> PanelSize.entries.find { it.name == raw } }
                        ?: defaults.panelSize,
                    transparentBackground = prefs[transparentKey] ?: defaults.transparentBackground,
                    compactMode = prefs[compactKey] ?: defaults.compactMode,
                    buttonX = prefs[buttonXKey] ?: defaults.buttonX,
                    buttonY = prefs[buttonYKey] ?: defaults.buttonY
                )
            }
            .distinctUntilChanged()

    override suspend fun setOpacity(opacity: Float) {
        dataStore.edit { it[opacityKey] = opacity.coerceIn(0.4f, 1.0f) }
    }

    override suspend fun setPanelSize(size: PanelSize) {
        dataStore.edit { it[panelSizeKey] = size.name }
    }

    override suspend fun setTransparentBackground(enabled: Boolean) {
        dataStore.edit { it[transparentKey] = enabled }
    }

    override suspend fun setCompactMode(enabled: Boolean) {
        dataStore.edit { it[compactKey] = enabled }
    }

    override suspend fun setButtonPosition(x: Int, y: Int) {
        dataStore.edit {
            it[buttonXKey] = x
            it[buttonYKey] = y
        }
    }
}
