package com.tftricks.app.data.repository

import com.tftricks.app.data.source.AssetJsonDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.DeserializationStrategy

/**
 * Base repository that loads a JSON list from assets once and keeps it in memory.
 * Assets never change at runtime, so the cache has no invalidation.
 */
abstract class CachedAssetRepository<T>(
    private val dataSource: AssetJsonDataSource,
    private val fileName: String,
    private val deserializer: DeserializationStrategy<List<T>>
) {
    private val mutex = Mutex()

    @Volatile
    private var cache: List<T>? = null

    protected suspend fun getAll(): List<T> =
        cache ?: mutex.withLock {
            cache ?: dataSource.load(fileName, deserializer).also { cache = it }
        }
}
