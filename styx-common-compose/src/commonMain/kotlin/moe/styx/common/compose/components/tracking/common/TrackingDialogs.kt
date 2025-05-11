package moe.styx.common.compose.components.tracking.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.michaelflisar.composedialogs.core.DialogButtonType
import com.michaelflisar.composedialogs.core.DialogEvent
import com.michaelflisar.composedialogs.core.rememberDialogState
import com.michaelflisar.composedialogs.dialogs.list.DialogList
import com.michaelflisar.composedialogs.dialogs.number.DialogNumberPicker
import com.michaelflisar.composedialogs.dialogs.number.NumberPickerSetup
import com.michaelflisar.composedialogs.dialogs.number.rememberDialogNumber
import moe.styx.common.extension.capitalize
import moe.styx.common.extension.eqI

@Composable
fun StatusDialog(current: String, available: List<String>, onDismiss: () -> Unit, onUpdate: (String) -> Unit) {
    val state = rememberDialogState(current)
    val selected = remember { mutableStateOf<Int?>(available.indexOfLast { it eqI current }) }
    DialogList(
        title = { Text("Watch status") },
        icon = { Icon(Icons.Filled.Checklist, "Watch status") },
        state = state,
        description = "Setting this to None will remove the entry from your list.",
        items = available,
        itemIdProvider = { available.indexOf(it) },
        selectionMode = DialogList.SelectionMode.SingleSelect(
            selected = selected,
            selectOnRadioButtonClickOnly = false
        ),
        itemContents = DialogList.ItemDefaultContent(
            text = { it.capitalize() }
        ),
        onEvent = {
            if (it.dismissed)
                onDismiss()
            if (it is DialogEvent.Button && it.button == DialogButtonType.Positive) {
                onUpdate(selected.value?.let { available[it] } ?: "")
            }
        }
    )
}

@Composable
fun EpisodeDialog(current: Int, max: Int?, onDismiss: () -> Unit, onUpdate: (Int) -> Unit) {
    val state = rememberDialogState()
    val value = rememberDialogNumber(current)
    DialogNumberPicker(
        state = state,
        title = { Text("Episode") },
        value = value,
        onEvent = {
            if (it.dismissed)
                onDismiss()
            if (it is DialogEvent.Button && it.button == DialogButtonType.Positive) {
                onUpdate(value.value)
            }
        },
        setup = NumberPickerSetup(
            min = 0, max = max ?: Int.MAX_VALUE, stepSize = 1
        )
    )
}