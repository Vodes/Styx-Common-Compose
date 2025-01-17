package moe.styx.common.compose.components.misc

import androidx.compose.animation.*
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@Composable
fun ScrollToTopContainer(
    modifier: Modifier = Modifier,
    scrollableState: ScrollableState,
    fabModifier: Modifier = Modifier,
    fabShape: Shape = FloatingActionButtonDefaults.shape,
    fabContainerColor: Color = FloatingActionButtonDefaults.containerColor,
    fabContentColor: Color = contentColorFor(fabContainerColor),
    fabElevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    content: @Composable BoxScope.() -> Unit
) {
    var showScrollUpButton by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(scrollableState.isScrollInProgress) {
        (scrollableState as? LazyListState)?.let { showScrollUpButton = it.firstVisibleItemIndex != 0 }
        (scrollableState as? LazyGridState)?.let { showScrollUpButton = it.firstVisibleItemIndex != 0 }
    }

    Box(modifier) {
        AnimatedVisibility(
            showScrollUpButton,
            modifier = Modifier.zIndex(2f).align(Alignment.BottomEnd).padding(20.dp),
            enter = expandIn() + fadeIn(),
            exit = shrinkOut() + fadeOut()
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        (scrollableState as? LazyListState)?.animateScrollToItem(0)
                        (scrollableState as? LazyGridState)?.animateScrollToItem(0)
                    }
                    showScrollUpButton = false
                },
                modifier = fabModifier,
                shape = fabShape,
                containerColor = fabContainerColor,
                elevation = fabElevation,
                contentColor = fabContentColor
            ) {
                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Scroll to top")
            }
        }
        content()
    }
}