package com.tftricks.app.ui.screens.settings

import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tftricks.app.TFTricksApplication
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.domain.model.PanelSize
import com.tftricks.app.overlay.OverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OverlaySettingsViewModel(private val app: TFTricksApplication) : ViewModel() {

    private val repository = app.container.overlayPrefsRepository

    val settings: StateFlow<OverlaySettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverlaySettings())

    val isOverlayRunning: StateFlow<Boolean> = OverlayService.isRunning

    private val _hasPermission = MutableStateFlow(Settings.canDrawOverlays(app))
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    /** Called on resume — the user may have just come back from the system settings page. */
    fun refreshPermission() {
        _hasPermission.value = Settings.canDrawOverlays(app)
    }

    fun startOverlay() {
        refreshPermission()
        if (_hasPermission.value) OverlayService.start(app)
    }

    fun stopOverlay() {
        OverlayService.stop(app)
    }

    fun setOpacity(opacity: Float) {
        viewModelScope.launch { repository.setOpacity(opacity) }
    }

    fun setPanelSize(size: PanelSize) {
        viewModelScope.launch { repository.setPanelSize(size) }
    }

    fun setTransparentBackground(enabled: Boolean) {
        viewModelScope.launch { repository.setTransparentBackground(enabled) }
    }

    fun setCompactMode(enabled: Boolean) {
        viewModelScope.launch { repository.setCompactMode(enabled) }
    }
}
