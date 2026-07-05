package com.tftricks.app.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.TierBadge
import com.tftricks.app.ui.screens.augments.color
import com.tftricks.app.ui.screens.augments.displayName
import com.tftricks.app.ui.screens.items.displayName
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

@Composable
fun SearchScreen(
    contentPadding: PaddingValues,
    onOpenComp: (String) -> Unit,
    onOpenChampion: (String) -> Unit,
    onOpenItem: (String) -> Unit,
    onOpenTraits: () -> Unit,
    onOpenAugments: () -> Unit,
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "search_field") {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search comps, champions, items…", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = OutlineDark,
                    cursorColor = BrandYellow,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        if (results.query.length >= 2 && results.isEmpty) {
            item(key = "no_results") {
                Text(
                    text = "Nothing found for \"${results.query}\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }

        if (results.comps.isNotEmpty()) {
            item(key = "comps_header") { SectionLabel(text = "Team Comps") }
            items(results.comps, key = { "comp_${it.id}" }) { comp ->
                InfoCard(modifier = Modifier.clickable { onOpenComp(comp.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TierBadge(comp.tier)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(comp.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
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

        if (results.champions.isNotEmpty()) {
            item(key = "champions_header") { SectionLabel(text = "Champions") }
            items(results.champions, key = { "champ_${it.id}" }) { champion ->
                InfoCard(modifier = Modifier.clickable { onOpenChampion(champion.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${champion.cost}g",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = costColor(champion.cost)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(champion.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(
                                text = champion.traits.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (results.traits.isNotEmpty()) {
            item(key = "traits_header") { SectionLabel(text = "Traits") }
            items(results.traits, key = { "trait_${it.id}" }) { trait ->
                InfoCard(modifier = Modifier.clickable(onClick = onOpenTraits)) {
                    Text(trait.name, style = MaterialTheme.typography.titleSmall, color = BrandYellow)
                    Text(
                        text = trait.champions.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        if (results.items.isNotEmpty()) {
            item(key = "items_header") { SectionLabel(text = "Items") }
            items(results.items, key = { "item_${it.id}" }) { item ->
                InfoCard(modifier = Modifier.clickable { onOpenItem(item.id) }) {
                    Text(item.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(
                        text = item.category.displayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        if (results.augments.isNotEmpty()) {
            item(key = "augments_header") { SectionLabel(text = "Augments") }
            items(results.augments, key = { "aug_${it.id}" }) { augment ->
                InfoCard(modifier = Modifier.clickable(onClick = onOpenAugments)) {
                    Text(
                        text = augment.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = augment.tier.color()
                    )
                    Text(
                        text = augment.tier.displayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
