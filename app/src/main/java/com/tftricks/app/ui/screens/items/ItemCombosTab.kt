package com.tftricks.app.ui.screens.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tftricks.app.ui.components.FilterChipRow
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary

/**
 * Component-pair explorer: pick a base component to see what it builds
 * with every other component (from the recipes in the item database).
 */
@Composable
fun ItemCombosTab(
    content: ItemsContent,
    onOpenItem: (String) -> Unit
) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val first = selected ?: content.components.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "picker") {
            Column {
                SectionLabel(text = "First component")
                FilterChipRow(
                    options = content.components,
                    isSelected = { it == first },
                    onToggle = { selected = it },
                    label = { it },
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        if (first != null) {
            item(key = "header") {
                SectionLabel(text = "$first + …")
            }
            items(content.components, key = { it }) { second ->
                val recipe = content.combos.find {
                    (it.componentA == first && it.componentB == second) ||
                        (it.componentA == second && it.componentB == first)
                }
                InfoCard(
                    onClick = recipe?.let { { onOpenItem(it.result.id) } }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = recipe?.result?.name ?: "no recipe",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (recipe != null) BrandYellow else TextSecondary
                        )
                    }
                    if (recipe != null) {
                        Text(
                            text = recipe.result.effect,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
