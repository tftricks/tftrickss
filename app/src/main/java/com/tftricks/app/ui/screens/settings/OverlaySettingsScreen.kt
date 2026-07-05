package com.tftricks.app.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.domain.model.PanelSize
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.FilterChipRow
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SuccessGreen
import com.tftricks.app.ui.theme.SurfaceElevated
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary

@Composable
fun OverlaySettingsScreen(
    contentPadding: PaddingValues,
    viewModel: OverlaySettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val isRunning by viewModel.isOverlayRunning.collectAsStateWithLifecycle()

    // Permission may change while we're in the system settings page — recheck on resume.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // On Android 13+ the foreground-service notification is only visible with
    // POST_NOTIFICATIONS; the overlay itself works either way, so start regardless.
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.startOverlay() }

    val startOverlay = {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.startOverlay()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Permission
        InfoCard {
            SectionLabel(text = "Overlay permission")
            if (hasPermission) {
                Text(
                    text = "Permission granted ✓",
                    style = MaterialTheme.typography.titleSmall,
                    color = SuccessGreen,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "TFTricks can draw the floating panel over your game.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    text = "TFTricks needs the \"Display over other apps\" permission to show " +
                        "the floating button and comp panel on top of TFT. Nothing is recorded " +
                        "or read from your screen — the overlay only draws on top of it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = PureBlack
                    ),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text("Grant permission")
                }
                Text(
                    text = "Denied it before? Open the settings page again and enable TFTricks " +
                        "in the list. The overlay stays disabled until then — the rest of the " +
                        "app works normally.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // 2. Service control
        InfoCard {
            SectionLabel(text = "Overlay service")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRunning) "Running" else "Stopped",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isRunning) SuccessGreen else TextSecondary
                    )
                    Text(
                        text = if (isRunning) "The floating button is on screen."
                        else "Start to show the floating button over your game.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Button(
                    onClick = { if (isRunning) viewModel.stopOverlay() else startOverlay() },
                    enabled = hasPermission || isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) SurfaceElevated else BrandYellow,
                        contentColor = if (isRunning) TextPrimary else PureBlack,
                        disabledContainerColor = OutlineDark,
                        disabledContentColor = TextSecondary
                    )
                ) {
                    Text(if (isRunning) "Stop" else "Start")
                }
            }
        }

        // 3. Customization (applied live while the overlay runs)
        SectionLabel(text = "Customization — applies live")

        InfoCard {
            Text(
                text = "Opacity  •  ${(settings.opacity * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            var sliderValue by remember(settings.opacity) {
                mutableFloatStateOf(settings.opacity)
            }
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { viewModel.setOpacity(sliderValue) },
                valueRange = 0.4f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = BrandYellow,
                    activeTrackColor = BrandYellow,
                    inactiveTrackColor = OutlineDark
                )
            )
        }

        InfoCard {
            Text(
                text = "Panel size",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            FilterChipRow(
                options = PanelSize.entries,
                isSelected = { it == settings.panelSize },
                onToggle = viewModel::setPanelSize,
                label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        InfoCard {
            SettingSwitchRow(
                title = "Transparent background",
                description = "See the game through the panel.",
                checked = settings.transparentBackground,
                onCheckedChange = viewModel::setTransparentBackground
            )
            SettingSwitchRow(
                title = "Compact mode",
                description = "Denser lists and smaller text.",
                checked = settings.compactMode,
                onCheckedChange = viewModel::setCompactMode
            )
        }

        // 4. Troubleshooting
        InfoCard {
            SectionLabel(text = "Overlay not showing?")
            Text(
                text = "• Some devices kill background services aggressively — exclude TFTricks " +
                    "from battery optimization in system settings.\n" +
                    "• Game/performance modes on some phones block overlays; disable them for TFT.\n" +
                    "• If the button disappears after a restart, just start the overlay again here.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureBlack,
                checkedTrackColor = BrandYellow,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceElevated
            )
        )
    }
}
