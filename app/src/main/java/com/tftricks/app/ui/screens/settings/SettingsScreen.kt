package com.tftricks.app.ui.screens.settings

import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.BuildConfig
import com.tftricks.app.data.remote.CommunityDragonStatus
import com.tftricks.app.data.remote.debugLabel
import com.tftricks.app.overlay.OverlayService
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.rememberCommunityDragonRepository
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.DangerRed
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SurfaceCard
import com.tftricks.app.ui.theme.SurfaceElevated
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onOpenOverlaySettings: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val favoritesCount by viewModel.favoritesCount.collectAsStateWithLifecycle()
    val overlayRunning by OverlayService.isRunning.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    val communityDragon = rememberCommunityDragonRepository()
    val communityDragonStatus by communityDragon.status.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Theme
        InfoCard {
            SectionLabel(text = "Appearance")
            Text(
                text = "Dark mode only",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "TFTricks ships with a single theme: pure black with the yellow accent. " +
                    "No light mode, by design.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Overlay quick toggle
        InfoCard {
            SectionLabel(text = "Overlay")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "In-game overlay",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = if (overlayRunning) "Running — floating button is on screen."
                        else "Show the floating panel over TFT.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = overlayRunning,
                    onCheckedChange = { wantOn ->
                        when {
                            !wantOn -> OverlayService.stop(context)
                            Settings.canDrawOverlays(context) -> OverlayService.start(context)
                            else -> onOpenOverlaySettings()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = BrandYellow,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = SurfaceElevated
                    )
                )
            }
            TextButton(onClick = onOpenOverlaySettings) {
                Text("Open overlay settings →", color = BrandYellow)
            }
        }

        // Data
        InfoCard {
            SectionLabel(text = "Data")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Clear favorites",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "$favoritesCount favorited entries across comps, champions, items and augments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Button(
                    onClick = { showClearDialog = true },
                    enabled = favoritesCount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = DangerRed,
                        disabledContainerColor = OutlineDark,
                        disabledContentColor = TextSecondary
                    )
                ) {
                    Text("Clear")
                }
            }
        }

        // Remove ads (future IAP)
        InfoCard {
            SectionLabel(text = "Support TFTricks")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Remove ads",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "One-time purchase — coming in a future update.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Button(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = OutlineDark,
                        disabledContentColor = TextSecondary
                    )
                ) {
                    Text("Soon")
                }
            }
        }

        // Links (placeholders)
        InfoCard {
            SectionLabel(text = "Links")
            LinkRow(title = "Website", subtitle = "Coming soon")
            LinkRow(title = "Discord community", subtitle = "Coming soon")
            LinkRow(title = "Privacy policy", subtitle = "Coming soon")
        }

        // About
        InfoCard {
            SectionLabel(text = "About")
            Text(
                text = "TFTricks ${viewModel.appVersion}",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "Teamfight Tactics companion. Team comp guides are original TFTricks " +
                    "content, bundled with the app. Champion, item, and trait data is fetched " +
                    "live from Riot's public CommunityDragon feed each session, so it stays " +
                    "current through patches automatically. No accounts, no tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "TFTricks was created under Riot Games' 'Legal Jibber Jabber' policy using " +
                    "assets owned by Riot Games. Riot Games does not endorse or sponsor this project.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // Debug-only diagnostics for CommunityDragon — never shown in release builds.
        if (BuildConfig.DEBUG) {
            InfoCard {
                SectionLabel(text = "Debug Info")
                Text(
                    text = "CommunityDragon: ${communityDragonStatus.debugLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )
                val successStatus = communityDragonStatus as? CommunityDragonStatus.Success
                Text(
                    text = "Detected set: ${successStatus?.setNumber?.toString() ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Champions loaded: ${successStatus?.championCount?.toString() ?: "—"}   " +
                        "Items loaded: ${successStatus?.itemCount?.toString() ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Button(
                    onClick = { communityDragon.forceRefresh() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = BrandYellow
                    ),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text("Force Refresh")
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = SurfaceCard,
            title = { Text("Clear all favorites?", color = TextPrimary) },
            text = {
                Text(
                    "This removes all $favoritesCount favorited comps, champions, items and " +
                        "augments. Saved teams from the Team Builder are kept.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllFavorites()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun LinkRow(title: String, subtitle: String) {
    // TODO: point these at real destinations when the sites exist.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}
