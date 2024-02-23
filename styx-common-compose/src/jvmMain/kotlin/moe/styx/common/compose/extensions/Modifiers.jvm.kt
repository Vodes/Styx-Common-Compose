package moe.styx.common.compose.extensions

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.desktopPointerEvent(onEnter: () -> Unit, onLeave: () -> Unit): Modifier {
    return this.onPointerEvent(PointerEventType.Enter) { onEnter() }.onPointerEvent(PointerEventType.Exit) { onLeave() }
}