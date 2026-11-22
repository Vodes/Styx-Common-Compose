package moe.styx.common.compose.threads

import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.styx.common.util.Log

abstract class LifecycleTrackedJob(val shouldBeStopped: Boolean = true) {
    internal var runJob: Boolean = true
    private var currentJob: Job? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private var pendingRestart: Job? = null

    private fun dispatch(action: () -> Unit) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            mutex.withLock {
                try {
                    action()
                } catch (exception: Exception) {
                    Log.e("LifecycleTrackedJob", exception) { "Failed to handle lifecycle event" }
                }
            }
        }
    }

    abstract fun createJob(): Job

    fun onLifecycleEvent(event: Lifecycle.Event) {
        dispatch { handleEvent(event) }
    }

    protected fun startTrackedJob() {
        dispatch { requestStart() }
    }

    private fun handleEvent(event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> requestStart()

            Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> {
                if (shouldBeStopped) {
                    runJob = false
                    pendingRestart = null
                    currentJob?.cancel()
                }
            }

            else -> {}
        }
    }

    private fun requestStart() {
        val job = currentJob
        if (job == null) {
            startJob()
        } else if (!job.isActive && pendingRestart !== job) {
            pendingRestart = job
            job.invokeOnCompletion {
                dispatch {
                    if (pendingRestart === job && currentJob === job) {
                        pendingRestart = null
                        startJob()
                    }
                }
            }
        }
    }

    private fun startJob() {
        runJob = true
        currentJob = createJob()
    }
}
