package moe.styx.common.compose.components.anime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.http.login
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Media
import moe.styx.common.data.MediaPreferences
import moe.styx.common.data.UserMediaPreferences
import moe.styx.common.util.launchThreaded

@Composable
fun MediaPreferencesIconButton(preferences: MediaPreferences?, media: Media, mainvm: MainDataViewModel) {
    var expanded by remember { mutableStateOf(false) }
    IconButtonWithTooltip(Icons.Default.Tune, "Open media preferences") {
        expanded = !expanded
    }
    MediaPreferencesDropdown(
        expanded = expanded,
        onDismiss = { expanded = false },
        preferences = preferences ?: MediaPreferences(null, null, null, null, null),
        onUpdatePrefs = { prefs ->
            val new = UserMediaPreferences(login?.userID ?: "", media.GUID, prefs)
            launchThreaded {
                RequestQueue.updateMediaPreference(new)?.first?.join()
                mainvm.updateData(true)
            }
        })
}

@Composable
fun MediaPreferencesDropdown(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismiss: () -> Unit,
    preferences: MediaPreferences,
    onUpdatePrefs: (MediaPreferences) -> Unit
) {
    DropdownMenu(expanded, onDismiss, modifier) {
        Text(
            "The non-checked mark indicates that it will fallback to your general settings.",
            Modifier.padding(8.dp, 4.dp).widthIn(0.dp, 380.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        HorizontalDivider(Modifier.padding(20.dp, 4.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
        TristateCheckboxItem("Auto-Sync enabled", preferences.autoSyncEnabled) { onUpdatePrefs(preferences.copy(autoSyncEnabled = it)) }
        TristateCheckboxItem("Sort episodes ascendingly", preferences.sortEpisodesAscendingly) {
            onUpdatePrefs(preferences.copy(sortEpisodesAscendingly = it))
        }

        HorizontalDivider(Modifier.padding(20.dp, 4.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)

        TristateCheckboxItem("Prefer German subs", preferences.preferGermanSub) {
            onUpdatePrefs(preferences.copy(preferGermanSub = it))
        }
        TristateCheckboxItem("Prefer German dub", preferences.preferGermanDub) {
            onUpdatePrefs(preferences.copy(preferGermanDub = it))
        }
        TristateCheckboxItem("Prefer English dub", preferences.preferEnglishDub) {
            onUpdatePrefs(preferences.copy(preferEnglishDub = it))
        }
    }
}

@Composable
fun TristateCheckboxItem(name: String, value: Boolean?, onValueChange: (Boolean?) -> Unit) {
    val state = if (value == null) ToggleableState.Indeterminate else (if (value) ToggleableState.On else ToggleableState.Off)
    Box(Modifier.width(IntrinsicSize.Max).padding(4.dp).clickable(onClick = { onValueChange(state.next().toBoolean()) })) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriStateCheckbox(state, onClick = {
                onValueChange(state.next().toBoolean())
            })
            Text(name)
        }
    }
}

fun ToggleableState.next() = when (this) {
    ToggleableState.Off -> ToggleableState.On
    ToggleableState.On -> ToggleableState.Indeterminate
    ToggleableState.Indeterminate -> ToggleableState.Off
}

fun ToggleableState.toBoolean() = when (this) {
    ToggleableState.On -> true
    ToggleableState.Off -> false
    ToggleableState.Indeterminate -> null
}