package com.tftricks.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tftricks.app.ui.theme.BrandYellow
import com.tftricks.app.ui.theme.PureBlack
import com.tftricks.app.ui.theme.SurfaceCard
import com.tftricks.app.ui.theme.TextSecondary

/** Horizontally scrollable row of toggleable filter chips. */
@Composable
fun <T> FilterChipRow(
    options: List<T>,
    isSelected: (T) -> Boolean,
    onToggle: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = isSelected(option),
                onClick = { onToggle(option) },
                label = { Text(label(option)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SurfaceCard,
                    labelColor = TextSecondary,
                    selectedContainerColor = BrandYellow,
                    selectedLabelColor = PureBlack
                )
            )
        }
    }
}
