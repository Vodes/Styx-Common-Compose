package moe.styx.common.compose.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ObserveLifecycleEvents(onEvent: (Lifecycle.Event) -> Unit) {
    val owner = LocalLifecycleOwner.current
    val currentOnEvent by rememberUpdatedState(onEvent)

    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> currentOnEvent(event) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
