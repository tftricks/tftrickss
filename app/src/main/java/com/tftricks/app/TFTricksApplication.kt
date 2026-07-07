package com.tftricks.app

import android.app.Application
import com.tftricks.app.di.AppContainer

class TFTricksApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.adsManager.initialize()
    }
}
