package com.tftricks.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/** Single Preferences DataStore holding all locally persisted user data. */
val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "tftricks_user_data")
