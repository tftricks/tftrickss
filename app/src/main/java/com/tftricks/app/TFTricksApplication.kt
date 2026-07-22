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
        container.communityDragonRepository.initialize()
    }

    /** Icons are loaded live from CommunityDragon each session; Coil's default in-memory
     *  + disk cache is enough here since there's no offline data behind them to protect. */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("icon_cache"))
                .maxSizePercent(0.05)
                .build()
        }
        .build()
}
