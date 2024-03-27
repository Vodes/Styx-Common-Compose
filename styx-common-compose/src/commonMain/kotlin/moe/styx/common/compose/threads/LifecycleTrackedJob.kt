package moe.styx.common.compose.threads

import com.multiplatform.lifecycle.LifecycleEvent
import kotlinx.coroutines.Job

abstract class LifecycleTrackedJob(val shouldBeStopped: Boolean = true) {
    internal var runJob: Boolean = true
    internal var currentJob: Job? = null

    abstract fun createJob(): Job

    fun onLifecycleEvent(event: LifecycleEvent) {
        when (event) {
            LifecycleEvent.OnResumeEvent -> {
                if (currentJob == null || currentJob?.isActive == false) {
                    runJob = true
                    currentJob = createJob()
                }
            }

            in arrayOf(LifecycleEvent.OnStopEvent, LifecycleEvent.OnDestroyEvent) -> {
                if (shouldBeStopped) {
                    runJob = false
                    currentJob = null
                }
            }

            else -> {}
        }
    }
}