package com.tftricks.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tftricks.app.domain.model.OverlaySession
import com.tftricks.app.domain.repository.OverlaySessionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

class DataStoreOverlaySessionRepository(
    private val dataStore: DataStore<Preferences>
) : OverlaySessionRepository {

    private val compIdKey = stringPreferencesKey("overlay_session_comp_id")
    private val centerScrollKey = intPreferencesKey("overlay_session_center_scroll")
    private val listIndexKey = intPreferencesKey("overlay_session_list_index")
    private val listOffsetKey = intPreferencesKey("overlay_session_list_offset")

    override suspend fun load(): OverlaySession {
        val prefs = dataStore.data.catch { emit(emptyPreferences()) }.first()
        return OverlaySession(
            selectedCompId = prefs[compIdKey],
            centerScrollOffset = prefs[centerScrollKey] ?: 0,
            listIndex = prefs[listIndexKey] ?: 0,
            listOffset = prefs[listOffsetKey] ?: 0
        )
    }

    override suspend fun save(session: OverlaySession) {
        dataStore.edit { prefs ->
            if (session.selectedCompId != null) {
                prefs[compIdKey] = session.selectedCompId
            } else {
                prefs.remove(compIdKey)
            }
            prefs[centerScrollKey] = session.centerScrollOffset
            prefs[listIndexKey] = session.listIndex
            prefs[listOffsetKey] = session.listOffset
        }
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(compIdKey)
            prefs.remove(centerScrollKey)
            prefs.remove(listIndexKey)
            prefs.remove(listOffsetKey)
        }
    }
}
