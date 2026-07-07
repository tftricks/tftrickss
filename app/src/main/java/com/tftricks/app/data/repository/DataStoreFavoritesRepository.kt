package com.tftricks.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.tftricks.app.domain.model.FavoriteCategory
import com.tftricks.app.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreFavoritesRepository(
    private val dataStore: DataStore<Preferences>
) : FavoritesRepository {

    private fun keyFor(category: FavoriteCategory): Preferences.Key<Set<String>> =
        stringSetPreferencesKey("favorites_${category.name.lowercase()}")

    override fun favorites(category: FavoriteCategory): Flow<Set<String>> =
        dataStore.data
            .map { prefs -> prefs[keyFor(category)] ?: emptySet() }
            .distinctUntilChanged()

    override suspend fun toggle(category: FavoriteCategory, id: String) {
        dataStore.edit { prefs ->
            val key = keyFor(category)
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (id in current) current - id else current + id
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { prefs ->
            FavoriteCategory.entries.forEach { prefs.remove(keyFor(it)) }
        }
    }
}
