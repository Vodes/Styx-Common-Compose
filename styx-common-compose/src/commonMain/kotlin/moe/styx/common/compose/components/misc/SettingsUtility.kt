package moe.styx.common.compose.components.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.AppShapes
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
        var switchValue by remember { mutableStateOf(value) }
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            modifier.padding(paddingValues).clip(clipShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                .clickable(interactionSource, null, enabled = enabled, onClick = {
                    switchValue = !switchValue
                    onValueChange(switchValue)
                }),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(10.dp).weight(1f), horizontalAlignment = Alignment.Start) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (!description.isNullOrBlank()) {
                    Text(description, Modifier.padding(1.dp, 3.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                Switch(switchValue, {
                    switchValue = it
                    onValueChange(switchValue)
                }, colors = switchColors, enabled = enabled)
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
        var currentValue by remember { mutableStateOf(value ?: choices.first()) }
        Column(modifier.padding(paddingValues).clip(clipShape).background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))) {
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.Start) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (!description.isNullOrBlank()) {
                    Text(description, Modifier.padding(4.dp), style = MaterialTheme.typography.bodySmall)
                }
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