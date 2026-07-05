package com.tftricks.app.ui.screens.augments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.domain.model.AugmentTier
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.TextSecondary

@Composable
fun AugmentsScreen(
    contentPadding: PaddingValues,
    viewModel: AugmentsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { augments ->
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
            items(augments, key = { it.id }) { augment ->
                val tierColor = when (augment.tier) {
                    AugmentTier.SILVER -> Color(0xFFB8B8C0)
                    AugmentTier.GOLD -> BrandYellow
                    AugmentTier.PRISMATIC -> Color(0xFF7FE7F0)
                }
                InfoCard {
                    Column {
                        Text(
                            text = augment.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = tierColor
                        )
                        Text(
                            text = "${augment.tier.name.lowercase().replaceFirstChar { it.uppercase() }} • " +
                                "Priority ${augment.priorityRating}/5",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
