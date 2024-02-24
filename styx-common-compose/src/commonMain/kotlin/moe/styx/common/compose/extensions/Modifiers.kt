package moe.styx.common.compose.extensions

import androidx.compose.ui.Modifier

expect fun Modifier.desktopPointerEvent(onEnter: () -> Unit, onLeave: () -> Unit): Modifier

expect fun Modifier.onRightClick(onClick: () -> Unit): Modifier