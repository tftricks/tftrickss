package com.tftricks.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tftricks.app.domain.model.SavedTeam
import com.tftricks.app.domain.repository.SavedTeamsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Persists built teams as a JSON list under a single preferences key. */
class DataStoreSavedTeamsRepository(
    private val dataStore: DataStore<Preferences>,
    private val json: Json
) : SavedTeamsRepository {

    private val key = stringPreferencesKey("saved_teams")
    private val serializer = ListSerializer(SavedTeam.serializer())

    override val savedTeams: Flow<List<SavedTeam>> =
        dataStore.data
            .map { prefs -> decode(prefs[key]) }
            .distinctUntilChanged()

    override suspend fun saveTeam(team: SavedTeam) {
        dataStore.edit { prefs ->
            val updated = decode(prefs[key]).filterNot { it.id == team.id } + team
            prefs[key] = json.encodeToString(serializer, updated)
        }
    }

    override suspend fun deleteTeam(id: String) {
        dataStore.edit { prefs ->
            val updated = decode(prefs[key]).filterNot { it.id == id }
            prefs[key] = json.encodeToString(serializer, updated)
        }
    }

    private fun decode(raw: String?): List<SavedTeam> =
        if (raw.isNullOrBlank()) emptyList()
        else runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList())
}
