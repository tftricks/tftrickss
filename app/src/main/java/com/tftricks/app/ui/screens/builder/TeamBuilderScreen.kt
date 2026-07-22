package com.tftricks.app.ui.screens.builder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tftricks.app.ui.AppViewModelProvider
import com.tftricks.app.ui.components.BoardCellData
import com.tftricks.app.ui.components.BoardGrid
import com.tftricks.app.ui.components.GameIcon
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.components.PillChip
import com.tftricks.app.ui.components.SectionLabel
import com.tftricks.app.ui.components.StateContent
import com.tftricks.app.ui.components.rememberCommunityDragonRepository
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.OutlineDark
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SuccessGreen
import com.tftricks.app.ui.theme.TextPrimary
import com.tftricks.app.ui.theme.TextSecondary
import com.tftricks.app.ui.theme.costColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamBuilderScreen(
    contentPadding: PaddingValues,
    viewModel: TeamBuilderViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var teamName by rememberSaveable { mutableStateOf("") }
    val communityDragon = rememberCommunityDragonRepository()
    val championIcons by communityDragon.championIconUrls.collectAsStateWithLifecycle()

    StateContent(state = state, modifier = Modifier.padding(contentPadding), onRetry = viewModel::retry) { content ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Board
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionLabel(text = "Board (${content.board.size} units)")
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = viewModel::clearBoard,
                    enabled = content.board.isNotEmpty()
                ) {
                    Text("Clear", color = if (content.board.isEmpty()) OutlineDark else TextSecondary)
                }
            }
            BoardGrid(
                cellFor = { position ->
                    content.board[position]?.let { champion ->
                        BoardCellData(
                            shortName = champion.name,
                            accentColor = costColor(champion.cost),
                            iconUrl = championIcons[champion.name]
                        )
                    }
                },
                selectedPosition = content.selectedCell,
                onCellClick = viewModel::onCellClick
            )
            Text(
                text = "Tap a cell to select it, tap a champion to place. Tap an occupied cell to remove.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            // Active traits
            SectionLabel(text = "Traits")
            if (content.traitStatuses.isEmpty()) {
                Text(
                    text = "Place champions to see trait breakpoints.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                InfoCard {
                    content.traitStatuses.forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(
                                text = status.count.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (status.isActive) BrandYellow else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = status.trait.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (status.isActive) TextPrimary else TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = status.trait.breakpoints.joinToString(" › ") { bp ->
                                    bp.count.toString()
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            status.activeBreakpointIndex?.let { index ->
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${status.trait.breakpoints[index].count} active)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BrandYellow
                                )
                            }
                        }
                    }
                }
            }

            // Roster
            SectionLabel(text = "Champions")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val placedIds = content.board.values.map { it.id }.toSet()
                content.roster.forEach { champion ->
                    val placed = champion.id in placedIds
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GameIcon(
                            url = championIcons[champion.name],
                            borderColor = costColor(champion.cost),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        PillChip(
                            text = "${champion.cost}g ${champion.name}",
                            contentColor = if (placed) PureBlack else costColor(champion.cost),
                            containerColor = if (placed) costColor(champion.cost)
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = { viewModel.onChampionClick(champion) }
                        )
                    }
                }
            }

            // Save
            SectionLabel(text = "Save team")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    placeholder = { Text("Team name", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandYellow,
                        unfocusedBorderColor = OutlineDark,
                        cursorColor = BrandYellow,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Button(
                    onClick = {
                        viewModel.saveTeam(teamName)
                        teamName = ""
                    },
                    enabled = teamName.isNotBlank() && content.board.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = PureBlack,
                        disabledContainerColor = OutlineDark,
                        disabledContentColor = TextSecondary
                    )
                ) {
                    Text("Save")
                }
            }
            content.savedMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = SuccessGreen
                )
            }
        }
    }
}
