package moe.styx.common.compose.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.get
import io.github.z4kn4fein.semver.toVersion
import kotlinx.coroutines.launch
import moe.styx.common.compose.AppContextImpl
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.compose.utils.fetchVersions
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

    var availablePreRelease by mutableStateOf<String?>(null)

    init {
        Log.d { "Initializing overview model" }
        runLoginAndChecks()
    }

    fun runLoginAndChecks() = screenModelScope.launch {
        runLogin()
        runChecks()
    }

    private suspend fun runLogin() {
        Log.d { "Checking login..." }
        if (isLoggedIn == null || isLoggedIn == false) {
            isLoggedIn = isLoggedIn()
            Log.d { "Logged in: $isLoggedIn | ${login?.name}" }
        }
    }

    open fun checkPlayerVersion() {}

    private fun runChecks() {
        screenModelScope.launch {
            launch versionCheck@{
                val versionCheckURL = AppContextImpl.appConfig().versionCheckURL
                if (versionCheckURL.isBlank())
                    return@versionCheck
                val versions = fetchVersions(versionCheckURL)
                versions.sortedDescending()
                val appVersion = AppContextImpl.appConfig().appVersion.toVersion(false)
                val latestVersion = versions.firstOrNull { !it.isPreRelease }
                val latestPreRelease = versions.firstOrNull { it.isPreRelease }
                if (latestVersion != null) {
                    if (appVersion < latestVersion) {
                        Log.i { "Found new version, enforcing update: $latestVersion" }
                        isOutdated = true
                    }
                }
                if (latestPreRelease != null) {
                    if (appVersion < latestPreRelease && isOutdated != true) {
                        if (settings["check-for-pre-release", true])
                            availablePreRelease = latestPreRelease.toString()
                        Log.i { "Found newer pre-release version! ($availablePreRelease)" }
                    }
                }
            }
            isOffline =
                ServerStatus.lastKnown in arrayOf(ServerStatus.OFFLINE, ServerStatus.UNKNOWN, ServerStatus.TIMEOUT)
            checkPlayerVersion()
        }
    }
}