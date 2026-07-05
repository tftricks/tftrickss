package com.tftricks.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.TFTricksLogo
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { overview ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TFTricksLogo(modifier = Modifier.padding(top = 8.dp))

            Text(
                text = "Patch ${overview.currentPatch} • offline data",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile("Champions", overview.championCount, Modifier.weight(1f))
                StatTile("Traits", overview.traitCount, Modifier.weight(1f))
                StatTile("Items", overview.itemCount, Modifier.weight(1f))
                StatTile("Augments", overview.augmentCount, Modifier.weight(1f))
            }

            Text(
                text = "Top Comps",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            overview.topComps.forEach { comp ->
                InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TierBadge(comp.tier)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = comp.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = comp.tags.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: Int, modifier: Modifier = Modifier) {
    InfoCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = BrandYellow
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
