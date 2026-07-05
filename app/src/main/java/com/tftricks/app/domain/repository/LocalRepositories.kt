package com.tftricks.app.domain.repository

import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.domain.model.PanelSize
import com.tftricks.app.domain.model.SavedTeam
import kotlinx.coroutines.flow.Flow

/** Locally persisted favorite ids per category. */
interface FavoritesRepository {
    fun favorites(category: FavoriteCategory): Flow<Set<String>>
    suspend fun toggle(category: FavoriteCategory, id: String)
}

/** Locally persisted teams built in the Team Builder. */
interface SavedTeamsRepository {
    val savedTeams: Flow<List<SavedTeam>>
    suspend fun saveTeam(team: SavedTeam)
    suspend fun deleteTeam(id: String)
}

/** Locally persisted overlay customization, observed live by the overlay service. */
interface OverlayPrefsRepository {
    val settings: Flow<OverlaySettings>
    suspend fun setOpacity(opacity: Float)
    suspend fun setPanelSize(size: PanelSize)
    suspend fun setTransparentBackground(enabled: Boolean)
    suspend fun setCompactMode(enabled: Boolean)
    suspend fun setButtonPosition(x: Int, y: Int)
}
