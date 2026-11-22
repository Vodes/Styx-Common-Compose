package moe.styx.common.compose.threads

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LifecycleTrackedJobTest {
    private class TestJob(shouldBeStopped: Boolean = true) : LifecycleTrackedJob(shouldBeStopped) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val started = Channel<Job>(Channel.UNLIMITED)
        val cleanupStarted = CompletableDeferred<Unit>()
        val finishCleanup = CompletableDeferred<Unit>()

        fun startDirectly() = startTrackedJob()

        override fun createJob(): Job = scope.launch(start = CoroutineStart.LAZY) {
            started.send(coroutineContext[Job]!!)
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    cleanupStarted.complete(Unit)
                    finishCleanup.await()
                }
            }
        }.also { it.start() }
    }

    @Test
    fun restartWaitsForCancelledJobCleanup() = runBlocking {
        val tracked = TestJob()
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        val first = withTimeout(5_000) { tracked.started.receive() }
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        tracked.onLifecycleEvent(Lifecycle.Event.ON_STOP)
        withTimeout(5_000) { tracked.cleanupStarted.await() }
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertNull(withTimeoutOrNull(100) { tracked.started.receive() })
        assertTrue(first.isCancelled)

        tracked.finishCleanup.complete(Unit)
        val second = withTimeout(5_000) { tracked.started.receive() }
        assertTrue(first.isCompleted)
        assertTrue(second.isActive)
        tracked.onLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    @Test
    fun stopDuringCleanupCancelsPendingRestart() = runBlocking {
        val tracked = TestJob()
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        withTimeout(5_000) { tracked.started.receive() }
        tracked.onLifecycleEvent(Lifecycle.Event.ON_STOP)
        withTimeout(5_000) { tracked.cleanupStarted.await() }
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        tracked.onLifecycleEvent(Lifecycle.Event.ON_STOP)
        tracked.finishCleanup.complete(Unit)
        assertNull(withTimeoutOrNull(200) { tracked.started.receive() })

        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        withTimeout(5_000) { tracked.started.receive() }
        tracked.onLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    @Test
    fun nonStoppingJobContinuesAcrossStopAndDestroy() = runBlocking {
        val tracked = TestJob(shouldBeStopped = false)
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        val job = withTimeout(5_000) { tracked.started.receive() }
        tracked.onLifecycleEvent(Lifecycle.Event.ON_STOP)
        tracked.onLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertNull(withTimeoutOrNull(200) { tracked.started.receive() })
        assertTrue(job.isActive)
        tracked.finishCleanup.complete(Unit)
        job.cancel()
    }

    @Test
    fun directStartsShareLifecycleSequencing() = runBlocking {
        val tracked = TestJob()
        tracked.startDirectly()
        val first = withTimeout(5_000) { tracked.started.receive() }
        tracked.startDirectly()
        tracked.onLifecycleEvent(Lifecycle.Event.ON_STOP)
        withTimeout(5_000) { tracked.cleanupStarted.await() }
        tracked.startDirectly()
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertNull(withTimeoutOrNull(100) { tracked.started.receive() })

        tracked.finishCleanup.complete(Unit)
        val second = withTimeout(5_000) { tracked.started.receive() }
        assertTrue(first.isCompleted)
        tracked.startDirectly()
        assertNull(withTimeoutOrNull(100) { tracked.started.receive() })
        assertTrue(second.isActive)
        tracked.onLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    @Test
    fun failedStartDoesNotStopLaterLifecycleEvents() = runBlocking {
        val attempts = Channel<Int>(Channel.UNLIMITED)
        val tracked = object : LifecycleTrackedJob() {
            var count = 0

            override fun createJob(): Job {
                attempts.trySend(++count)
                if (count == 1) error("Failed to start")
                return Job()
            }
        }

        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        tracked.onLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, withTimeout(5_000) { attempts.receive() })
        assertEquals(2, withTimeout(5_000) { attempts.receive() })
        tracked.onLifecycleEvent(Lifecycle.Event.ON_STOP)
    }
}
