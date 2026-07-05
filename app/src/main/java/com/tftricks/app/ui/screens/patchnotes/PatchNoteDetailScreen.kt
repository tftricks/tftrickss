package com.tftricks.app.ui.screens.patchnotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary

@Composable
fun PatchNoteDetailScreen(
    contentPadding: PaddingValues,
    viewModel: PatchNoteDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { patch ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoCard {
                Text(
                    text = "Patch ${patch.version}",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandYellow
                )
                Text(
                    text = patch.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = patch.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            SectionLabel(text = "Changes")
            patch.changes.forEach { change ->
                InfoCard {
                    Text(
                        text = change.target,
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandYellow
                    )
                    Text(
                        text = change.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
