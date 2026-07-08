package com.tftricks.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tftricks.app.TFTricksApplication
import com.tftricks.app.data.remote.DataDragonRepository

/** Grabs the app-wide [DataDragonRepository] the same way other screens reach into [AppContainer]. */
@Composable
fun rememberDataDragonRepository(): DataDragonRepository {
    val context = LocalContext.current
    return remember {
        (context.applicationContext as TFTricksApplication).container.dataDragonRepository
    }
}
