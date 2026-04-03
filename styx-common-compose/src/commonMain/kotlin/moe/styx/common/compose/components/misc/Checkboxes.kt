package moe.styx.common.compose.components.misc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.russhwolf.settings.get
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.LocalIsTv

@Composable
fun SettingsCheckbox(
    title: String,
    key: String,
    default: Boolean,
    paddingValues: PaddingValues = PaddingValues(10.dp),
    description: String = "",
    onUpdate: (Boolean) -> Unit = {}
) {
    var setting by rememberSaveable { mutableStateOf(settings[key, default]) }

    if (description.isBlank())
        Row(modifier = Modifier.height(45.dp).padding(paddingValues)) {
            TextWithCheckBox(title, setting, Modifier.align(Alignment.CenterVertically)) { updated ->
                setting = updated.also { settings.putBoolean(key, updated) }.also { onUpdate(updated) }
            }
        }
    else
        Column(Modifier.height(85.dp).padding(paddingValues)) {
            Row {
                TextWithCheckBox(title, setting, Modifier.align(Alignment.CenterVertically)) { updated ->
                    setting = updated.also { settings.putBoolean(key, updated) }.also { onUpdate(updated) }
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
}

@Composable
fun TextWithCheckBox(title: String, value: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true, onUpdate: (Boolean) -> Unit = {}) {
    val isTv = LocalIsTv.current
    var isFocused by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    fun toggleValue() {
        onUpdate(!value)
        if (isTv) {
            scope.launch { focusRequester.requestFocus() }
        }
    }

    Surface(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent {
                if (
                    isTv &&
                    enabled &&
                    it.type == KeyEventType.KeyUp &&
                    it.key in arrayOf(Key.Enter, Key.NumPadEnter, Key.DirectionCenter)
                ) {
                    toggleValue()
                    true
                } else {
                    false
                }
            }
            .focusable(enabled = enabled, interactionSource = interactionSource)
            .clickable(interactionSource, null, enabled = enabled && !isTv) { toggleValue() }
            .then(
                if (isTv) {
                    Modifier.border(
                        2.dp,
                        when {
                            !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                            isFocused -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                        },
                        AppShapes.large
                    )
                } else {
                    Modifier
                }
            ),
        shape = AppShapes.large,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(
            if (isTv) {
                if (isFocused) 3.dp else 1.dp
            } else {
                3.dp
            }
        ),
        tonalElevation = if (isTv) {
            if (isFocused) 2.dp else 0.dp
        } else {
            0.dp
        },
        shadowElevation = 0.dp
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Checkbox(
                checked = value, enabled = enabled, onCheckedChange = if (isTv) null else { updated -> onUpdate(updated) },
                modifier = Modifier.focusProperties {
                    canFocus = false
                }
            )
        }
    }
}
