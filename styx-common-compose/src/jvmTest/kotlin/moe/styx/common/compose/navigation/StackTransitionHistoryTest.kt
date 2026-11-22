package moe.styx.common.compose.navigation

import io.github.hristogochev.vortex.stack.StackEvent
import io.github.hristogochev.vortex.stack.mutableStateStackOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StackTransitionHistoryTest {
    @Test
    fun initialAndAlreadyHandledStacksDoNotReplayStalePopEvents() {
        val history = StackTransitionHistory(listOf("root", "detail"))

        assertEquals(StackTransition.None, history.onStackChanged(listOf("root", "detail"), StackEvent.Pop))
        assertEquals(StackTransition.Back, history.onStackChanged(listOf("root"), StackEvent.Pop))
        assertEquals(StackTransition.None, history.onStackChanged(listOf("root"), StackEvent.Pop))
    }

    @Test
    fun completedGestureDoesNotAnimateAgainButLaterPopDoes() {
        val stack = mutableStateStackOf("root", "first", "second", minSize = 1)
        val history = StackTransitionHistory(stack.items)

        assertTrue(stack.pop())
        history.syncToStack(stack.items)
        assertEquals(StackTransition.None, history.onStackChanged(stack.items, stack.lastEvent))

        assertTrue(stack.pop())
        assertEquals(StackTransition.Back, history.onStackChanged(stack.items, stack.lastEvent))
    }

    @Test
    fun failedGesturePopDoesNotSuppressNextProgrammaticPop() {
        val stack = mutableStateStackOf("root", minSize = 1)
        val history = StackTransitionHistory(stack.items)

        assertFalse(stack.pop())
        assertTrue(history.isHandled(stack.items))
        stack.push("detail")
        assertEquals(StackTransition.Forward, history.onStackChanged(stack.items, stack.lastEvent))
        assertTrue(stack.pop())
        assertEquals(StackTransition.Back, history.onStackChanged(stack.items, stack.lastEvent))
    }

    @Test
    fun subsequentPushAndSameSizedReplacementAreHandled() {
        val history = StackTransitionHistory(listOf("root", "first", "second"))

        history.syncToStack(listOf("root", "first"))
        assertEquals(StackTransition.Forward, history.onStackChanged(listOf("root", "first", "third"), StackEvent.Push))
        assertEquals(StackTransition.Forward, history.onStackChanged(listOf("root", "first", "fourth"), StackEvent.Replace))
        assertEquals(StackTransition.Back, history.onStackChanged(listOf("root", "first"), StackEvent.Pop))
    }

    @Test
    fun interruptionResynchronizesLatestStackWithoutReplayingItsTransition() {
        val stack = mutableStateStackOf("root", "first", minSize = 1)
        val history = StackTransitionHistory(stack.items)

        stack.push("second")
        assertEquals(StackTransition.Forward, history.onStackChanged(stack.items, stack.lastEvent))
        assertTrue(stack.pop())
        history.syncToStack(stack.items)
        assertEquals(StackTransition.None, history.onStackChanged(stack.items, stack.lastEvent))

        stack.push("third")
        assertEquals(StackTransition.Forward, history.onStackChanged(stack.items, stack.lastEvent))
    }
}
