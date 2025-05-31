package moe.styx.common.compose.components.tracking.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.michaelflisar.composedialogs.core.Dialog
import com.michaelflisar.composedialogs.core.DialogButtonType
import com.michaelflisar.composedialogs.core.DialogEvent
import com.michaelflisar.composedialogs.core.rememberDialogState
import com.michaelflisar.composedialogs.dialogs.list.DialogList
import com.michaelflisar.composedialogs.dialogs.number.DialogNumberPicker
import com.michaelflisar.composedialogs.dialogs.number.NumberPickerSetup
import com.michaelflisar.composedialogs.dialogs.number.rememberDialogNumber
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.extension.capitalize
import moe.styx.common.extension.eqI
import moe.styx.common.extension.roundToDecimals
import kotlin.math.max
import kotlin.math.min

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

@Composable
fun NumberDialog(
    title: String,
    icon: ImageVector,
    valueIn: Float,
    min: Float,
    max: Float,
    step: Float,
    representAsInt: Boolean,
    onUpdate: (Float?) -> Unit
) {
    val dialogState = rememberDialogState()
    var state by remember {
        mutableStateOf(
            TextFieldValue(valueIn.toString())
        )
    }
    LaunchedEffect(state) {
        val floatVal = state.text.toFloatOrNull() ?: 0.0F
        if (floatVal < min || floatVal > max) {
            state = state.copy(text = max(min(floatVal, max), min).toString())
        }
    }
    Dialog(
        dialogState,
        icon = {
            Icon(icon, "$title Dialog")
        },
        title = {
            Text(title)
        },
        onEvent = {
            val currentValue = state.text.toFloatOrNull() ?: 0.0F
            if (it.dismissed) {
                onUpdate(if (currentValue != valueIn) currentValue else null)
            }
        }) {
        Column(Modifier.fillMaxWidth(0.6F), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val stepStr = if (representAsInt) step.toInt() else step
                IconButtonWithTooltip(Icons.Default.Remove, "Reduce by $stepStr") {
                    state = state.copy(text = (state.text.toFloat() - step).toString())
                }
                Spacer(Modifier.weight(1f))
                OutlinedTextField(
                    value = state.copy(text = if (representAsInt) state.text.toFloat().toInt().toString() else state.text),
                    onValueChange = { newState ->
                        state = newState
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = if (!representAsInt) KeyboardType.Decimal else KeyboardType.Number),
                    modifier = Modifier.width(140.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                )
                Spacer(Modifier.weight(1f))
                IconButtonWithTooltip(Icons.Default.Add, "Increase by $stepStr") {
                    state = state.copy(text = (state.text.toFloat() + step).toString())
                }
            }
            Slider(
                value = state.text.toFloatOrNull() ?: 0.0F,
                onValueChange = { newVal ->
                    if (!representAsInt) {
                        val value = newVal.toDouble().roundToDecimals(1)
                        state = state.copy(text = value.toString())
                    } else {
                        state = state.copy(text = newVal.toString())
                    }
                },
                modifier = Modifier.padding(7.dp).fillMaxWidth(),
                valueRange = min..max,
                steps = (if (!representAsInt) max * 10F else max / step).toInt(),
                colors = SliderDefaults.colors(
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    activeTickColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            )
        }
    }
}