package moe.styx.common.compose.threads

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import moe.styx.common.Platform
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.login
import moe.styx.common.compose.http.sendObjectWithResponse
import moe.styx.common.data.ActiveUser
import moe.styx.common.data.ClientHeartbeat
import moe.styx.common.data.MediaActivity
import moe.styx.common.extension.eqI
import moe.styx.common.json
import moe.styx.common.util.Log
import moe.styx.common.util.launchGlobal

object Heartbeats : LifecycleTrackedJob() {
    val currentUserState: MutableStateFlow<List<ActiveUser>> = MutableStateFlow(emptyList())
    var mediaActivity: MediaActivity? = null
    var listeningTo: String? = null
    var runAswell: (() -> Unit)? = null

    override fun createJob(): Job = launchGlobal {
        while (runJob) {
            delay(3000)
            runAswell?.let { it() }
            if (login == null) {
                delay(10000)
                continue
            }
            runCatching {
                val response = sendObjectWithResponse(Endpoints.HEARTBEAT, ClientHeartbeat(login!!.accessToken, mediaActivity, listeningTo))
                if (response != null && response.code == 200 && !response.message.isNullOrBlank()) {
                    val list = json.decodeFromString<List<ActiveUser>>(response.message!!).sortedBy { it.user.name.lowercase() }
                    currentUserState.emit(list)
                }
            }
            if (listeningTo != null && currentUserState.value.find { it.user.GUID eqI listeningTo } == null)
                listeningTo = null

            // TODO: Once we establish the use of `listeningTo` we should also only wait 5000 if that's given
            if (mediaActivity == null)
                delay(7000)
            else
                delay(3000)
        }
    }

    fun start() {
        if (Platform.current != Platform.JVM) {
            Log.w("Heartbeats::start") { "This function is not designed for use outside of Desktop applications." }
            return
        }
        runJob = true
        currentJob = createJob()
    }
}