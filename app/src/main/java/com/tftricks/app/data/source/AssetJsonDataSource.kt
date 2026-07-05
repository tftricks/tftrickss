package com.tftricks.app.data.source

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

/**
 * Reads and parses JSON files bundled under `assets/data/`.
 * This is the app's only data source — TFTricks is fully offline.
 */
class AssetJsonDataSource(
    private val context: Context,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun <T> load(fileName: String, deserializer: DeserializationStrategy<T>): T =
        withContext(ioDispatcher) {
            val text = context.assets.open("$DATA_DIR/$fileName")
                .bufferedReader()
                .use { it.readText() }
            json.decodeFromString(deserializer, text)
        }

    private companion object {
        const val DATA_DIR = "data"
    }
}
