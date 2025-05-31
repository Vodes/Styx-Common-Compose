package moe.styx.common.compose.components.tracking.common.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.tracking.common.ElevatedSurface
import moe.styx.common.compose.components.tracking.common.NumberDialog
import moe.styx.common.extension.padString
import pw.vodes.anilistkmp.graphql.type.ScoreFormat

@Composable
fun RatingComponent(scoreIn: Float, scoreFormat: ScoreFormat, isEnabled: Boolean = true, onUpdate: (Float) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showInlineOptions by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(scoreIn) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ElevatedSurface(
            Modifier.padding(3.dp),
            enabled = isEnabled,
            onClick = {
                when (scoreFormat) {
                    ScoreFormat.POINT_3, ScoreFormat.POINT_5 -> showInlineOptions = !showInlineOptions
                    else -> showDialog = true
                }
            }
        ) {
            Column(Modifier.widthIn(150.dp, Dp.Unspecified), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.padding(4.dp).defaultMinSize(Dp.Unspecified, if (scoreFormat == ScoreFormat.POINT_3) 33.dp else Dp.Unspecified),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingSurfaceContent(score, scoreFormat)
                }
            }
        }
        AnimatedVisibility(showInlineOptions) {
            InlineScoreOptions(score, scoreFormat, isEnabled) {
                score = it
                if (score != scoreIn)
                    onUpdate(score)
            }
        }
    }

    if (showDialog && isEnabled) {
        val maxValue = when (scoreFormat) {
            ScoreFormat.POINT_10, ScoreFormat.POINT_10_DECIMAL -> 10F
            else -> 100F
        }
        val step = when (scoreFormat) {
            ScoreFormat.POINT_10_DECIMAL -> 0.5F
            ScoreFormat.POINT_10 -> 1F
            else -> 5F
        }
        NumberDialog("Rating", Icons.Filled.RateReview, score, 0F, maxValue, step, scoreFormat != ScoreFormat.POINT_10_DECIMAL) {
            showDialog = false
            it?.let {
                score = it
                if (score != scoreIn)
                    onUpdate(score)
            }
        }
    }
}

@Composable
internal fun RatingSurfaceContent(score: Float, scoreFormat: ScoreFormat) {
    Icon(Icons.Filled.RateReview, "Review", modifier = Modifier.padding(2.dp, 4.dp, 2.dp, 1.dp))
    if (score == 0.0F) {
        Text("Rate", modifier = Modifier.padding(4.dp, 0.dp), style = MaterialTheme.typography.labelLarge)
    } else {
        when (scoreFormat) {
            ScoreFormat.POINT_10, ScoreFormat.POINT_100, ScoreFormat.POINT_10_DECIMAL -> {
                val visibleScore = if (scoreFormat != ScoreFormat.POINT_10_DECIMAL)
                    score.toInt().padString(if (scoreFormat == ScoreFormat.POINT_100) 3 else 1)
                else
                    score
                val maxScore = when (scoreFormat) {
                    ScoreFormat.POINT_100 -> 100
                    ScoreFormat.POINT_10 -> 10
                    ScoreFormat.POINT_10_DECIMAL -> 10.0
                    else -> 0
                }
                Text(
                    "$visibleScore / $maxScore",
                    Modifier.padding(4.dp, 1.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            ScoreFormat.POINT_5 -> {
                Row(Modifier.padding(4.dp, 1.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.padding(3.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(score.toInt().toString(), style = MaterialTheme.typography.labelLarge)
                    }
                    Text("/", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.padding(3.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("5", style = MaterialTheme.typography.labelLarge)
                        Icon(Icons.Filled.Star, "Maximum stars", Modifier.size(18.dp))
                    }
                }
            }

            ScoreFormat.POINT_3 -> {
                Row(Modifier.padding(4.dp, 1.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Mood:", style = MaterialTheme.typography.labelLarge)
                    Point3ScoreIcon(score)
                }
            }

            else -> {}
        }
    }
}