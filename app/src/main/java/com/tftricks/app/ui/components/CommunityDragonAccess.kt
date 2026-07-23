package com.tftricks.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tftricks.app.TFTricksApplication
import com.tftricks.app.data.remote.CommunityDragonRepository

/** Grabs the app-wide [CommunityDragonRepository] the same way other screens reach into [AppContainer]. */
@Composable
fun rememberCommunityDragonRepository(): CommunityDragonRepository {
    val context = LocalContext.current
    return remember {
        (context.applicationContext as TFTricksApplication).container.communityDragonRepository
    }
}
