package moe.styx.common.compose.components.tracking.common.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pw.vodes.anilistkmp.graphql.type.ScoreFormat

@Composable
fun Point3ScoreIcon(score: Float) {
    when {
        score.toInt() == 1 -> {
            Icon(
                Icons.Default.SentimentVeryDissatisfied,
                "Dissatisfied",
                Modifier.padding(3.dp, 1.dp, 2.dp, 0.dp).size(30.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        score.toInt() == 2 -> {
            Icon(
                Icons.Default.SentimentNeutral,
                "Neutral",
                Modifier.padding(3.dp, 1.dp, 2.dp, 0.dp).size(30.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        else -> {
            Icon(
                Icons.Default.SentimentVerySatisfied,
                "Satisfied",
                Modifier.padding(3.dp, 1.dp, 2.dp, 0.dp).size(30.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RowScope.InlineScoreOptions(score: Float, scoreFormat: ScoreFormat, isEnabled: Boolean = true, onChanged: (Float) -> Unit) {
    val iconSizeModifier = Modifier.padding(4.dp, 1.dp).let {
        if (scoreFormat == ScoreFormat.POINT_3)
            it.size(40.dp)
        else
            it.size(40.dp, 36.dp)
    }.clip(CardDefaults.elevatedShape)
    Row {
        if (scoreFormat == ScoreFormat.POINT_3) {
            (1..3).forEach {
                Surface(
                    iconSizeModifier.clickable(isEnabled) {
                        if (it.toFloat() == score || !isEnabled)
                            return@clickable
                        onChanged(it.toFloat())
                    },
                    color = if (score.toInt() == it) MaterialTheme.colorScheme.surfaceColorAtElevation(24.dp) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp
                ) {
                    Point3ScoreIcon(it.toFloat())
                }
            }
        } else {
            (1..5).forEach {
                Surface(
                    iconSizeModifier.clickable(isEnabled) {
                        if (it.toFloat() == score || !isEnabled)
                            return@clickable
                        onChanged(it.toFloat())
                    },
                    color = if (score.toInt() == it) MaterialTheme.colorScheme.surfaceColorAtElevation(24.dp) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(it.toString(), Modifier.padding(4.dp, 0.dp, 1.dp, 0.dp), style = MaterialTheme.typography.labelLarge)
                        Icon(Icons.Filled.Star, "Rate $it stars", Modifier.size(18.dp))
                    }
                }
            }
        }
        AnimatedVisibility(score != 0F) {
            Surface(
                iconSizeModifier.clickable(enabled = score != 0F && isEnabled) {
                    onChanged(0F)
                },
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp
            ) {
                Icon(
                    Icons.Default.Delete, "Delete Rating",
                    modifier = Modifier.padding(4.dp, 1.dp, 2.dp, 0.dp).size(30.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}