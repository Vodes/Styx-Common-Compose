package moe.styx.common.compose.extensions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Pointer enter and leave event modifier. No-Op on mobile but still available in common for convenience.
 *
 * @param onEnter   Function to be called when mouse cursor enters this component
 * @param onLeave   Function to be called when mouse cursor leaves this component
 */
expect fun Modifier.desktopPointerEvent(onEnter: () -> Unit, onLeave: () -> Unit): Modifier

/**
 * A clickable Modifier that handles normal clicks, long press and right clicks, with the latter being jvm only.
 *
 * @param regularClick  Function to be called on a normal click
 * @param otherClick    Function to be called when long-pressed or right-clicked
 */
expect fun Modifier.dynamicClick(regularClick: () -> Unit = {}, otherClick: () -> Unit = {}): Modifier

@Composable
fun Modifier.clickableNoIndicator(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(interactionSource, null, enabled) {
        onClick()
    }
}