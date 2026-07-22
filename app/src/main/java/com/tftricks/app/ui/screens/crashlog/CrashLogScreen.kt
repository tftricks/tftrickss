package com.tftricks.app.ui.screens.crashlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.tftricks.app.crash.CrashHandler
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.theme.DangerRed
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.TextSecondary

/**
 * Debug-only: shows the last uncaught exception recorded by [CrashHandler], so a crash
 * repro can be diagnosed from the real stack trace instead of guessing from a description.
 */
@Composable
fun CrashLogScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    var crashText by remember { mutableStateOf(CrashHandler.readLastCrash(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val current = crashText
        if (current == null) {
            InfoCard {
                Text(
                    text = "No crash recorded",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
            }
        } else {
            InfoCard {
                SectionLabel(text = "Last crash")
                Text(
                    text = current,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Button(
                onClick = {
                    CrashHandler.clear(context)
                    crashText = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = PureBlack)
            ) {
                Text("Clear crash log")
            }
        }
    }
}
