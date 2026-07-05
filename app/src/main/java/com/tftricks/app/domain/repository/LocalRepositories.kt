package com.tftricks.app.domain.repository

import com.tftricks.app.domain.model.FavoriteCategory
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
