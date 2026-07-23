package com.tftricks.app.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.tftricks.app.domain.model.TeamComp
import com.tftricks.app.domain.model.toTeamPlannerCode
import com.tftricks.app.domain.repository.TeamPlannerRepository
import com.tftricks.app.ui.theme.SurfaceElevated
import com.tftricks.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private const val TAG = "CopyCompButton"

/**
 * Copies [comp] to the clipboard as a TFT Team Planner paste code. Fetches (and caches,
 * via [teamPlannerRepository]) the current set's champion code dictionary on first tap.
 * A champion with no known code just gets skipped (logged, "00") instead of failing
 * the whole copy — see [com.tftricks.app.domain.model.toTeamPlannerCode].
 */
@Composable
fun CopyCompButton(
    comp: TeamComp,
    teamPlannerRepository: TeamPlannerRepository,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SurfaceElevated)
            .clickable {
                scope.launch {
                    val encoding = teamPlannerRepository.getTeamPlannerEncoding()
                    if (encoding == null) {
                        Toast.makeText(context, "Couldn't load Team Planner data — try again", Toast.LENGTH_SHORT)
                            .show()
                        return@launch
                    }
                    val code = comp.toTeamPlannerCode(encoding) { unmatched ->
                        Log.w(TAG, "No Team Planner code for champion '$unmatched' in comp '${comp.name}'")
                    }
                    clipboard.setText(AnnotatedString(code))
                    Toast.makeText(context, "Comp copied — paste in TFT Team Planner", Toast.LENGTH_SHORT).show()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📋",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}
