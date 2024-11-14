package moe.styx.common.compose.components.misc

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
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
        ElevatedCard(Modifier.animateContentSize().padding(4.dp).clickable {
            lines = if (lines != maxLines) maxLines else Int.MAX_VALUE
        }, elevation = CardDefaults.elevatedCardElevation(2.dp)) {
            Text(text, modifier.padding(5.dp), maxLines = lines, overflow = TextOverflow.Ellipsis, style = style)
        }
    } else {
        Text(
            text,
            modifier.animateContentSize().clickable { lines = if (lines != maxLines) maxLines else Int.MAX_VALUE },
            maxLines = lines,
            overflow = TextOverflow.Ellipsis,
            style = style
        )
    }

}