package moe.styx.common.compose.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.http.login
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.util.Log

class ListPosViewModel : ScreenModel {
    var scrollIndex: Int by mutableStateOf(0)
    var scrollOffset: Int by mutableStateOf(0)
}

abstract class OverviewViewModel : ScreenModel {
    var isLoggedIn by mutableStateOf<Boolean?>(null)
        private set

    var isOffline by mutableStateOf<Boolean?>(null)
        private set

    var isOutdated by mutableStateOf<Boolean?>(null)
        private set

    init {
        screenModelScope.launch { runLogin() }
    }

    private fun runLogin() {
        Log.d { "Checking login..." }
        if (isLoggedIn == null) {
            isLoggedIn = isLoggedIn()
            Log.d { "Logged in: $isLoggedIn | ${login?.name}" }
        }
    }

    abstract fun isUpToDate(): Boolean
    open fun checkPlayerVersion() {}

    fun runChecks() {
        screenModelScope.launch {
            launch { isOutdated = !isUpToDate() }
            isOffline = ServerStatus.lastKnown in arrayOf(ServerStatus.OFFLINE, ServerStatus.UNKNOWN, ServerStatus.TIMEOUT)
            checkPlayerVersion()
        }
    }
}