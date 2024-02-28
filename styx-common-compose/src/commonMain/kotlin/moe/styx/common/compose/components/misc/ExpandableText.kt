package moe.styx.common.compose.components.misc

import androidx.compose.foundation.clickable
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    useCard: Boolean = true
) {
    var lines by remember { mutableStateOf(maxLines) }
    if (useCard) {
        ElevatedCard(Modifier.clickable {
            lines = if (lines != maxLines) maxLines else Int.MAX_VALUE
        }, elevation = CardDefaults.elevatedCardElevation(2.dp)) {
            Text(text, modifier, maxLines = lines, overflow = TextOverflow.Ellipsis, style = style)
        }
    } else {
        Text(
            text,
            modifier.clickable { lines = if (lines != maxLines) maxLines else Int.MAX_VALUE },
            maxLines = lines,
            overflow = TextOverflow.Ellipsis,
            style = style
        )
    }

}