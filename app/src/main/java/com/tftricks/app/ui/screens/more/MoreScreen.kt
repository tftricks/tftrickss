package com.tftricks.app.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tftricks.app.ui.components.InfoCard
import com.tftricks.app.ui.navigation.Destination
import com.tftricks.app.ui.theme.BrandYellow

/** Hub for secondary screens that don't fit in the bottom bar. */
@Composable
fun MoreScreen(
    contentPadding: PaddingValues,
    onNavigate: (Destination) -> Unit
) {
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
        items(Destination.moreDestinations, key = { it.route }) { destination ->
            InfoCard(onClick = { onNavigate(destination) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                        tint = BrandYellow
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
