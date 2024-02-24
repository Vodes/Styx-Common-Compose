package moe.styx.common.compose.threads

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.login
import moe.styx.common.compose.http.sendObjectWithResponse
import moe.styx.common.data.ActiveUser
import moe.styx.common.data.ClientHeartbeat
import moe.styx.common.data.MediaActivity
import moe.styx.common.extension.eqI
import moe.styx.common.json
import moe.styx.common.util.launchGlobal

object Heartbeats : LifecycleTrackedJob() {
    var currentUsers: List<ActiveUser> = emptyList()
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
                    currentUsers = json.decodeFromString(response.message!!)
                    currentUsers = currentUsers.sortedBy { it.user.name.lowercase() }
                }
            }
            if (listeningTo != null && currentUsers.find { it.user.GUID eqI listeningTo } == null)
                listeningTo = null

            // TODO: Once we establish the use of `listeningTo` we should also only wait 5000 if that's given
            if (mediaActivity == null)
                delay(7000)
            else
                delay(3000)
        }
    }
}