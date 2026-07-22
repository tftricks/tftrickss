package com.tftricks.app.domain.repository

import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.model.OverlaySession
import com.tftricks.app.domain.model.OverlaySettings
import com.tftricks.app.domain.model.SavedTeam
import kotlinx.coroutines.flow.Flow

/** Locally persisted favorite ids per category. */
interface FavoritesRepository {
    fun favorites(category: FavoriteCategory): Flow<Set<String>>
    suspend fun toggle(category: FavoriteCategory, id: String)

    /** Removes every favorite in every category. */
    suspend fun clearAll()
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
    suspend fun setCompactMode(enabled: Boolean)
    suspend fun setButtonPosition(x: Int, y: Int)
}

/**
 * The overlay panel's last-seen screen, persisted separately from [OverlayPrefsRepository]
 * so collapse/scroll snapshots don't ride the same flow that drives the window's
 * WindowManager layout updates. Written once on collapse (not per-scroll-frame) and
 * cleared when the overlay service is fully stopped.
 */
interface OverlaySessionRepository {
    suspend fun load(): OverlaySession
    suspend fun save(session: OverlaySession)
    suspend fun clear()
}
