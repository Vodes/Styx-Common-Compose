package moe.styx.common.compose.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier

actual fun Modifier.desktopPointerEvent(onEnter: () -> Unit, onLeave: () -> Unit): Modifier = this

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.dynamicClick(regularClick: () -> Unit, otherClick: () -> Unit): Modifier {
    return this.combinedClickable(onClick = regularClick, onLongClick = otherClick)
}