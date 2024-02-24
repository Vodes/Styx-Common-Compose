package moe.styx.common.compose.extensions

import androidx.compose.ui.Modifier

actual fun Modifier.desktopPointerEvent(onEnter: () -> Unit, onLeave: () -> Unit): Modifier = this

actual fun Modifier.onRightClick(onClick: () -> Unit): Modifier = this