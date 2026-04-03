package moe.styx.common.compose.components.misc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.ExpandIconButton
import moe.styx.common.compose.components.misc.Toggles.settingsContainer
import moe.styx.common.compose.utils.LocalIsTv
import moe.styx.common.extension.eqI

object Toggles {
    val colEndPadding: PaddingValues = PaddingValues(8.dp, 4.dp, 8.dp, 7.dp)
    val rowStartPadding: PaddingValues = PaddingValues(8.dp, 4.dp, 4.dp, 4.dp)
    val rowEndPadding: PaddingValues = PaddingValues(4.dp, 4.dp, 8.dp, 4.dp)

    @Composable
    fun Modifier.settingsContainer() =
        this.padding(5.dp).clip(AppShapes.large).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))

    @Composable
    fun ContainerSwitch(
        title: String,
        description: String? = null,
        modifier: Modifier = Modifier.fillMaxWidth(),
        paddingValues: PaddingValues = PaddingValues(8.dp, 4.dp),
        switchColors: SwitchColors = SwitchDefaults.colors(),
        clipShape: Shape = AppShapes.large,
        enabled: Boolean = true,
        value: Boolean,
        onValueChange: (Boolean) -> Unit
    ) {
        val isTv = LocalIsTv.current
        var switchValue by remember(value) { mutableStateOf(value) }
        var isFocused by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        val scope = rememberCoroutineScope()
        val interactionSource = remember { MutableInteractionSource() }

        fun toggleValue() {
            switchValue = !switchValue
            onValueChange(switchValue)
            if (isTv) {
                scope.launch { focusRequester.requestFocus() }
            }
        }

        Surface(
            modifier = modifier.padding(paddingValues)
                .clip(clipShape)
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
                .clickable(interactionSource, null, enabled = enabled && !isTv, onClick = { toggleValue() })
                .then(
                    if (isTv) {
                        Modifier.border(
                            2.dp,
                            when {
                                !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                                isFocused -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                            },
                            clipShape
                        )
                    } else {
                        Modifier
                    }
                ),
            shape = clipShape,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(
                if (isTv) {
                    if (isFocused) 4.dp else 3.dp
                } else {
                    3.dp
                }
            ),
            tonalElevation = if (isTv) {
                if (isFocused) 2.dp else 1.dp
            } else {
                0.dp
            },
            shadowElevation = 0.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.padding(10.dp).weight(1f), horizontalAlignment = Alignment.Start) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    if (!description.isNullOrBlank()) {
                        Text(description, Modifier.padding(1.dp, 3.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                    Switch(
                        switchValue,
                        if (isTv) null else { updated ->
                            switchValue = updated
                            onValueChange(switchValue)
                        },
                        colors = switchColors,
                        enabled = enabled,
                        modifier = Modifier.focusProperties {
                            canFocus = false
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun ContainerRadioSelect(
        title: String,
        description: String? = null,
        modifier: Modifier = Modifier.fillMaxWidth(),
        paddingValues: PaddingValues = PaddingValues(8.dp, 4.dp),
        clipShape: Shape = AppShapes.large,
        value: String? = null,
        choices: List<String>,
        onValueChange: (String) -> Unit
    ) {
        val isTv = LocalIsTv.current
        var currentValue by remember(value, choices) { mutableStateOf(value ?: choices.first()) }
        Column(modifier.padding(paddingValues).clip(clipShape).background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))) {
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.Start) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (!description.isNullOrBlank()) {
                    Text(description, Modifier.padding(4.dp), style = MaterialTheme.typography.bodySmall)
                }
                if (isTv) {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        choices.forEach { choice ->
                            val interactionSource = remember { MutableInteractionSource() }
                            var isFocused by remember { mutableStateOf(false) }
                            val focusRequester = remember { FocusRequester() }
                            val scope = rememberCoroutineScope()
                            Surface(
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .onPreviewKeyEvent {
                                        if (
                                            it.type == KeyEventType.KeyUp &&
                                            it.key in arrayOf(Key.Enter, Key.NumPadEnter, Key.DirectionCenter)
                                        ) {
                                            currentValue = choice
                                            onValueChange(currentValue)
                                            scope.launch { focusRequester.requestFocus() }
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                    .focusable(interactionSource = interactionSource)
                                    .clickable(interactionSource, null, enabled = !isTv) {
                                        currentValue = choice
                                        onValueChange(currentValue)
                                    }
                                    .border(
                                        2.dp,
                                        if (isFocused) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                                        },
                                        AppShapes.large
                                    ),
                                shape = AppShapes.large,
                                color = MaterialTheme.colorScheme.surfaceColorAtElevation(if (isFocused) 4.dp else 2.dp),
                                tonalElevation = if (isFocused) 2.dp else 0.dp
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(choice, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    RadioButton(
                                        selected = currentValue eqI choice,
                                        onClick = null,
                                        modifier = Modifier.focusProperties { canFocus = false }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    FlowRow(Modifier.padding(4.dp), verticalArrangement = Arrangement.Top) {
                        for (choice in choices) {
                            val interactionSource = remember { MutableInteractionSource() }
                            Row(Modifier.padding(8.dp, 4.dp).clickable(interactionSource, null) {
                                currentValue = choice
                                onValueChange(currentValue)
                            }, verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = currentValue eqI choice, onClick = {
                                    currentValue = choice
                                    onValueChange(currentValue)
                                })
                                Text(choice, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSettings(
    title: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    withContainer: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit)
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        Modifier.padding(5.dp).clickable(interactionSource, null) {
            onExpandClick()
        },
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        shadowElevation = 1.dp
    ) {
        Column {
            Row(Modifier.fillMaxWidth().padding(3.dp)) {
                Text(title, modifier = Modifier.padding(5.dp).weight(1f), style = MaterialTheme.typography.headlineSmall)
                ExpandIconButton(isExpanded = isExpanded, onClick = onExpandClick)
            }
            AnimatedVisibility(isExpanded) {
                if (withContainer) {
                    Column(Modifier.settingsContainer()) {
                        content()
                    }
                } else {
                    Column(Modifier.fillMaxWidth()) {
                        content()
                    }
                }
            }
        }
    }
}
