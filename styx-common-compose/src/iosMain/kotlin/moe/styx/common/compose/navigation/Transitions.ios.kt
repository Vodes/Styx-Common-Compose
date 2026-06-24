package moe.styx.common.compose.navigation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.hristogochev.vortex.screen.CurrentScreenNoTransitionsDisposable
import io.github.hristogochev.vortex.screen.render
import io.github.hristogochev.vortex.stack.StackEvent
import io.github.hristogochev.vortex.util.BackHandler
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.roundToInt

private const val BackgroundDisplacementFraction = 0.3f
private const val PositionalCompletionThreshold = 0.28f
private const val VelocityCompletionFraction = 3.5f
private const val DestinationInputReleaseThreshold = 0.08f

@Composable
actual fun StyxCurrentScreenPredictiveBack(
    navigator: Navigator,
    slideAnimationSpec: FiniteAnimationSpec<IntOffset>,
    enabled: Boolean,
    content: @Composable (Screen) -> Unit
) {
    StyxCurrentScreenIOSSwipe(
        navigator = navigator,
        enabled = enabled,
        content = content
    )
}

@Composable
private fun StyxCurrentScreenIOSSwipe(
    navigator: Navigator,
    enabled: Boolean,
    draggableHandlePadding: PaddingValues = PaddingValues(top = 80.dp),
    draggableHandleWidth: Dp = 24.dp,
    draggableHandleFillMaxHeight: Boolean = true,
    content: @Composable (Screen) -> Unit
) {
    CurrentScreenNoTransitionsDisposable(navigator)

    BackHandler(
        enabled = enabled && navigator.canPop && navigator.current.canPop,
        onBack = { navigator.pop() }
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        var stableWidthPx by remember { mutableFloatStateOf(maxWidthPx) }
        val stableWidthPxUpdated by rememberUpdatedState(stableWidthPx)

        val anchors by remember(stableWidthPx) {
            derivedStateOf {
                DraggableAnchors {
                    DismissValue.Start at 0f
                    DismissValue.End at stableWidthPx
                }
            }
        }

        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = DismissValue.Start,
                anchors = anchors
            )
        }

        val offset = anchoredDraggableState.offset
        val isGestureInFlight by remember(offset, stableWidthPx) {
            derivedStateOf { offset > 0f && offset < stableWidthPx }
        }

        var autoSwipe by remember { mutableStateOf(false) }

        LaunchedEffect(maxWidthPx, isGestureInFlight, autoSwipe) {
            if (!isGestureInFlight && !autoSwipe) {
                stableWidthPx = maxWidthPx
            }
        }

        val flingBehavior = iosFlingBehavior(
            state = anchoredDraggableState,
            positionalThreshold = { distance -> distance * PositionalCompletionThreshold },
            velocityThreshold = { stableWidthPxUpdated / VelocityCompletionFraction },
            snapAnimationSpec = SpringSpec(stiffness = Spring.StiffnessLow),
            decayAnimationSpec = exponentialDecay()
        )

        LaunchedEffect(anchors) {
            anchoredDraggableState.updateAnchors(anchors)
        }

        val last2NavigatorItems by remember(navigator.items) {
            derivedStateOf { navigator.items.takeLast(2) }
        }

        val screens = remember {
            when (last2NavigatorItems.size) {
                1 -> mutableStateListOf(last2NavigatorItems[0])
                else -> mutableStateListOf(last2NavigatorItems[0], last2NavigatorItems[1])
            }
        }

        val lastEventUpdated by rememberUpdatedState(navigator.lastEvent)

        LaunchedEffect(last2NavigatorItems) {
            when (lastEventUpdated) {
                StackEvent.Push, StackEvent.Replace -> {
                    autoSwipe = true

                    if (screens.size == 2) {
                        screens.removeAt(0)
                    }

                    anchoredDraggableState.snapTo(DismissValue.End)
                    syncScreens(listOf(screens.last(), last2NavigatorItems.last()), screens)

                    snapshotFlow { autoSwipe }.first { !it }

                    anchoredDraggableState.animateTo(DismissValue.Start, SpringSpec(stiffness = Spring.StiffnessLow))
                    syncScreens(last2NavigatorItems, screens)
                }

                StackEvent.Pop -> {
                    autoSwipe = true

                    syncScreens(listOf(last2NavigatorItems.last(), screens.last()), screens)
                    anchoredDraggableState.animateTo(DismissValue.End, SpringSpec(stiffness = Spring.StiffnessLow))

                    snapshotFlow { autoSwipe }.first { !it }

                    anchoredDraggableState.snapTo(DismissValue.Start)
                    syncScreens(last2NavigatorItems, screens)
                }

                StackEvent.PopGesture -> {
                    syncScreens(last2NavigatorItems, screens)
                    anchoredDraggableState.snapTo(DismissValue.Start)
                }

                StackEvent.Idle -> {
                    syncScreens(last2NavigatorItems, screens)
                }
            }
        }

        LaunchedEffect(offset, stableWidthPx) {
            if (screens.size == 1 || offset != stableWidthPx) return@LaunchedEffect

            if (autoSwipe) {
                autoSwipe = false
                return@LaunchedEffect
            }

            navigator.popGesture()
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            screens.forEachIndexed { index, screen ->
                val isBackground = screens.size == 2 && index == 0
                val isForeground = screens.size == 2 && index == 1

                val offsetModifier = when {
                    screens.size == 1 -> Modifier
                    isBackground -> Modifier
                        .offset {
                            IntOffset(calculateBackgroundScreenOffset(offset, stableWidthPx).roundToInt(), 0)
                        }
                        .drawWithContent {
                            drawContent()
                            val transitionFraction = (offset / stableWidthPx).coerceIn(0f, 1f)
                            drawRect(
                                color = Color.Black,
                                alpha = 0.25f - (transitionFraction * 0.25f)
                            )
                        }

                    isForeground -> Modifier.offset { IntOffset(offset.roundToInt(), 0) }
                    else -> error("Screen size must be either 1 or 2")
                }

                val draggableModifier = when {
                    screens.size == 1 -> Modifier
                    isBackground -> Modifier
                    isForeground && enabled && navigator.current.canPop -> Modifier.anchoredDraggable(
                        state = anchoredDraggableState,
                        orientation = Orientation.Horizontal,
                        flingBehavior = flingBehavior
                    )

                    else -> Modifier
                }

                val render by remember(screens.size, index, offset, stableWidthPx) {
                    derivedStateOf {
                        when {
                            screens.size == 1 -> true
                            isBackground -> offset != 0f
                            isForeground -> offset != stableWidthPx
                            else -> error("Screen size must be either 1 or 2")
                        }
                    }
                }

                val acceptsInput = when {
                    screens.size == 1 -> true
                    screen.key != navigator.current.key -> false
                    isForeground -> offset <= stableWidthPx * DestinationInputReleaseThreshold
                    isBackground -> offset >= stableWidthPx * (1f - DestinationInputReleaseThreshold)
                    else -> error("Screen size must be either 1 or 2")
                }

                key(screen.key) {
                    if (render) {
                        screen.render {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .zIndex(index.toFloat())
                                    .then(offsetModifier)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .zIndex(0f)
                                            .fillMaxSize()
                                    ) {
                                        content(it)
                                    }

                                    if (!acceptsInput) {
                                        Box(
                                            modifier = Modifier
                                                .zIndex(1f)
                                                .fillMaxSize()
                                                .clickable(
                                                    interactionSource = null,
                                                    indication = null,
                                                    onClick = {}
                                                )
                                        )
                                    }
                                }

                                Box(
                                    Modifier
                                        .let { if (draggableHandleFillMaxHeight) it.fillMaxHeight() else it }
                                        .width(draggableHandleWidth)
                                        .widthIn(min = draggableHandleWidth)
                                        .padding(draggableHandlePadding)
                                        .then(draggableModifier),
                                    propagateMinConstraints = false
                                ) {
                                    // Empty edge handle.
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class DismissValue {
    Start,
    End
}

private fun syncScreens(source: List<Screen>, state: SnapshotStateList<Screen>) {
    if (source.size !in 1..2 || state.size !in 1..2) return

    if (source.size == 1) {
        val target = source[0]
        when (state.size) {
            1 -> if (state[0].key != target.key) state[0] = target
            2 -> {
                when {
                    state[0].key == target.key && state[1].key != target.key -> state.removeAt(1)
                    state[1].key == target.key && state[0].key != target.key -> {
                        state.swap(0, 1)
                        state.removeAt(1)
                    }

                    else -> {
                        state[0] = target
                        state.removeAt(1)
                    }
                }
            }
        }
        return
    }

    val target0 = source[0]
    val target1 = source[1]

    if (state.size == 1) {
        when (state[0].key) {
            target0.key -> state.add(target1)
            target1.key -> state.add(0, target0)
            else -> {
                state[0] = target0
                state.add(target1)
            }
        }
        return
    }

    if (state[0].key != target0.key && state[1].key == target0.key) {
        state.swap(0, 1)
    }
    if (state[0].key != target0.key) {
        state[0] = target0
    }
    if (state[1].key != target1.key) {
        state[1] = target1
    }
}

private fun <T> SnapshotStateList<T>.swap(i: Int, j: Int) {
    if (i == j) return
    val temp = this[i]
    this[i] = this[j]
    this[j] = temp
}

private fun calculateBackgroundScreenOffset(
    currentOffset: Float,
    screenWidth: Float
): Float {
    val maxDisplacement = screenWidth * BackgroundDisplacementFraction
    val fraction = (currentOffset / screenWidth).coerceIn(0f, 1f)
    return -maxDisplacement + fraction * maxDisplacement
}

@Composable
private fun <T> iosFlingBehavior(
    state: AnchoredDraggableState<T>,
    positionalThreshold: (totalDistance: Float) -> Float,
    velocityThreshold: () -> Float,
    snapAnimationSpec: AnimationSpec<Float>,
    decayAnimationSpec: DecayAnimationSpec<Float>
): TargetedFlingBehavior {
    return remember(
        state,
        positionalThreshold,
        velocityThreshold,
        snapAnimationSpec,
        decayAnimationSpec
    ) {
        snapFlingBehavior(
            decayAnimationSpec = decayAnimationSpec,
            snapAnimationSpec = snapAnimationSpec,
            snapLayoutInfoProvider = anchoredDraggableLayoutInfoProvider(
                state = state,
                positionalThreshold = positionalThreshold,
                velocityThreshold = velocityThreshold
            )
        )
    }
}

private fun <T> anchoredDraggableLayoutInfoProvider(
    state: AnchoredDraggableState<T>,
    positionalThreshold: (totalDistance: Float) -> Float,
    velocityThreshold: () -> Float
): SnapLayoutInfoProvider =
    object : SnapLayoutInfoProvider {
        override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float = 0f

        override fun calculateSnapOffset(velocity: Float): Float {
            val currentOffset = state.requireOffset()
            val target = state.anchors.computeTarget(
                currentOffset = currentOffset,
                velocity = velocity,
                positionalThreshold = positionalThreshold,
                velocityThreshold = velocityThreshold
            )
            return state.anchors.positionOf(target) - currentOffset
        }
    }

private fun <T> DraggableAnchors<T>.computeTarget(
    currentOffset: Float,
    velocity: Float,
    positionalThreshold: (totalDistance: Float) -> Float,
    velocityThreshold: () -> Float
): T {
    require(!currentOffset.isNaN()) { "The offset provided to computeTarget must not be NaN." }

    val isMoving = abs(velocity) > 0.0f
    val isMovingForward = isMoving && velocity > 0f

    return if (!isMoving) {
        closestAnchor(currentOffset)!!
    } else if (abs(velocity) >= abs(velocityThreshold())) {
        closestAnchor(currentOffset, searchUpwards = isMovingForward)!!
    } else {
        val left = closestAnchor(currentOffset, searchUpwards = false)!!
        val leftAnchorPosition = positionOf(left)
        val right = closestAnchor(currentOffset, searchUpwards = true)!!
        val rightAnchorPosition = positionOf(right)
        val distance = abs(leftAnchorPosition - rightAnchorPosition)
        val relativeThreshold = abs(positionalThreshold(distance))
        val closestAnchorFromStart = if (isMovingForward) leftAnchorPosition else rightAnchorPosition
        val relativePosition = abs(closestAnchorFromStart - currentOffset)

        if (relativePosition >= relativeThreshold) {
            if (isMovingForward) right else left
        } else {
            if (isMovingForward) left else right
        }
    }
}
