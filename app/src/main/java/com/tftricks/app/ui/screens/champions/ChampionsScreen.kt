package com.tftricks.app.ui.screens.champions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.theme.Cost1
import com.tftricks.app.ui.theme.Cost2
import com.tftricks.app.ui.theme.Cost3
import com.tftricks.app.ui.theme.Cost4
import com.tftricks.app.ui.theme.Cost5
import com.tftricks.app.ui.theme.TextSecondary

@Composable
fun ChampionsScreen(
    contentPadding: PaddingValues,
    viewModel: ChampionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding)) { champions ->
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
            items(champions, key = { it.id }) { champion ->
                val costColor = when (champion.cost) {
                    1 -> Cost1
                    2 -> Cost2
                    3 -> Cost3
                    4 -> Cost4
                    else -> Cost5
                }
                InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${champion.cost}g",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = costColor
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = champion.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${champion.traits.joinToString(" • ")} — ${champion.role}",
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
