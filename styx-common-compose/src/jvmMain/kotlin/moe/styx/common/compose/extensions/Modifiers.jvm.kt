package moe.styx.common.compose.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.onClick
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.desktopPointerEvent(onEnter: () -> Unit, onLeave: () -> Unit): Modifier {
    return this.onPointerEvent(PointerEventType.Enter) { onEnter() }.onPointerEvent(PointerEventType.Exit) { onLeave() }
}

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.dynamicClick(regularClick: () -> Unit, otherClick: () -> Unit): Modifier {
    return this.combinedClickable(onClick = regularClick, onLongClick = otherClick)
        .onClick(true, matcher = PointerMatcher.mouse(PointerButton.Secondary), onClick = otherClick)
}