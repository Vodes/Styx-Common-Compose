package moe.styx.common.compose.navigation

import io.github.hristogochev.vortex.stack.StackEvent

internal enum class StackTransition {
    None,
    Forward,
    Back,
    Sync
}

internal class StackTransitionHistory<T>(initialStack: List<T>) {
    private var handledStack = initialStack.toList()

    fun isHandled(stack: List<T>): Boolean = stack == handledStack

    fun onStackChanged(stack: List<T>, event: StackEvent): StackTransition {
        if (isHandled(stack)) return StackTransition.None
        handledStack = stack.toList()
        return when (event) {
            StackEvent.Push, StackEvent.Replace -> StackTransition.Forward
            StackEvent.Pop -> StackTransition.Back
            StackEvent.Idle -> StackTransition.Sync
        }
    }

    fun syncToStack(stack: List<T>) {
        handledStack = stack.toList()
    }
}
