package moe.styx.common.compose.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.common.compose.AppContextImpl
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.http.login
import moe.styx.common.compose.navigation.ScreenModel
import moe.styx.common.compose.navigation.screenModelScope
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.compose.utils.fetchVersions
import moe.styx.common.util.Log

class ListPosViewModel : ScreenModel {
    var scrollIndex: Int by mutableStateOf(0)
    var scrollOffset: Int by mutableStateOf(0)
    var focusedKey: String by mutableStateOf("")
    var restoreFocus: Boolean by mutableStateOf(false)
}

data class VersionState(
    val installed: Version,
    val latest: Version?,
    val latestPreRelease: Version?,
) {
    init {
        Log.i { this.toString() }
    }

    fun canUpdate() = latest?.let { installed < it } == true || latestPreRelease?.let { installed < it } == true
    fun shouldForceUpdate() = latest?.let { installed < it } == true

    val availableUpdate: String
        get() {
            if (latest != null && latestPreRelease != null) {
                return (latest.takeIf { latest > latestPreRelease } ?: latestPreRelease).toString()
            }
            return (latest ?: latestPreRelease)?.toString() ?: ""
        }
    var toastShown = false
}

abstract class OverviewViewModel : ScreenModel {
    var isLoggedIn by mutableStateOf<Boolean?>(null)
        private set

    var isOffline by mutableStateOf<Boolean?>(null)
        private set

    var versionState by mutableStateOf<VersionState?>(null)
        private set

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
                var versions = fetchVersions(versionCheckURL)
                var attempts = 0;
                while (versions.isEmpty() && attempts < 3) {
                    delay(5000)
                    versions = fetchVersions(versionCheckURL)
                    attempts++
                }
                versions.sortedDescending()
                val appVersion = AppContextImpl.appConfig().appVersion.toVersion(false)
                val latestVersion = versions.firstOrNull { !it.isPreRelease }
                val latestPreRelease = versions.firstOrNull { it.isPreRelease }
                versionState = VersionState(appVersion, latestVersion, latestPreRelease)
            }
            isOffline =
                ServerStatus.lastKnown in arrayOf(ServerStatus.OFFLINE, ServerStatus.UNKNOWN, ServerStatus.TIMEOUT)
            checkPlayerVersion()
        }
    }
}
