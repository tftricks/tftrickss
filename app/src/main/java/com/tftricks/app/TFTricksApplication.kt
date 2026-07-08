package com.tftricks.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.tftricks.app.di.AppContainer

class TFTricksApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.adsManager.initialize()
        container.dataDragonRepository.initialize()
    }

    /**
     * Ignores server cache-control headers so champion/item icons persist to disk on first
     * load and keep working with the overlay/app fully offline afterward.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .respectCacheHeaders(false)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("icon_cache"))
                .maxSizePercent(0.05)
                .build()
        }
        .build()
}
