package com.tftricks.app.ui.screens.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.domain.model.Item
import com.tftricks.app.domain.model.ItemCategory
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.BannerAdSlot
import com.tftricks.app.ui.components.FavoriteButton
import com.tftricks.app.ui.components.FilterChipRow
import com.tftricks.app.ui.components.GameIcon
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.rememberDataDragonRepository
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.TextSecondary

fun ItemCategory.displayName(): String = when (this) {
    ItemCategory.AD -> "AD"
    ItemCategory.AP -> "AP"
    ItemCategory.TANK -> "Tank"
    ItemCategory.UTILITY -> "Utility"
    ItemCategory.ATTACK_SPEED -> "Attack Speed"
    ItemCategory.MANA -> "Mana"
}

@Composable
fun ItemsScreen(
    contentPadding: PaddingValues,
    onOpenItem: (String) -> Unit,
    viewModel: ItemsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val dataDragon = rememberDataDragonRepository()
    val itemIcons by dataDragon.itemIconUrls.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { content ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = PureBlack,
                contentColor = BrandYellow
            ) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text("Database") },
                    selectedContentColor = BrandYellow,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("Combos") },
                    selectedContentColor = BrandYellow,
                    unselectedContentColor = TextSecondary
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                when (tab) {
                    0 -> ItemDatabaseTab(content, viewModel, itemIcons, onOpenItem)
                    else -> ItemCombosTab(content, onOpenItem)
                }
            }
            BannerAdSlot(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
        }
    }
}

@Composable
private fun ItemDatabaseTab(
    content: ItemsContent,
    viewModel: ItemsViewModel,
    itemIcons: Map<String, String>,
    onOpenItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "category_filter") {
            FilterChipRow(
                options = ItemCategory.entries,
                isSelected = { it == content.selectedCategory },
                onToggle = viewModel::selectCategory,
                label = { it.displayName() }
            )
        }
        if (content.items.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "No items in this category.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
        items(content.items, key = { it.id }) { item ->
            ItemCard(
                item = item,
                iconUrl = itemIcons[item.id],
                isFavorite = item.id in content.favoriteIds,
                onClick = { onOpenItem(item.id) },
                onToggleFavorite = { viewModel.toggleFavorite(item.id) }
            )
        }
    }
}

@Composable
private fun ItemCard(
    item: Item,
    iconUrl: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    InfoCard(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            GameIcon(url = iconUrl, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${item.category.displayName()} • " +
                        if (item.components.isEmpty()) "Component"
                        else item.components.joinToString(" + "),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            FavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite)
        }
    }
}
