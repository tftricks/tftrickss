package com.tftricks.app.ui.screens.traits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.ExpandableSection
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TraitsScreen(
    contentPadding: PaddingValues,
    onOpenChampion: (String) -> Unit,
    viewModel: TraitsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding), onRetry = viewModel::retry) { content ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(content.traits, key = { it.id }) { trait ->
                ExpandableSection(
                    title = "${trait.name}  (${trait.breakpoints.joinToString("/") { it.count.toString() }})"
                ) {
                    Text(
                        text = trait.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    SectionLabel(text = "Breakpoints", modifier = Modifier.padding(top = 10.dp))
                    trait.breakpoints.forEach { breakpoint ->
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = "(${breakpoint.count})",
                                style = MaterialTheme.typography.titleSmall,
                                color = BrandYellow
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = breakpoint.effect,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                    SectionLabel(text = "Champions", modifier = Modifier.padding(top = 10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        trait.champions.forEach { name ->
                            val champion = content.championsByName[name]
                            PillChip(
                                text = name,
                                contentColor = costColor(champion?.cost ?: 1),
                                onClick = champion?.let { { onOpenChampion(it.id) } }
                            )
                        }
                    }
                }
            }
        }
    }
}
