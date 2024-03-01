package moe.styx.common.compose.components.misc

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.extension.eqI

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StringChoices(
    title: String,
    choices: List<String>,
    description: String? = null,
    value: String? = null,
    onUpdate: (String) -> String
) {
    val value = value ?: choices[0]
    var selected by mutableStateOf(value)
    Column(Modifier.padding(10.dp, 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        FlowRow(verticalArrangement = Arrangement.Top) {
            for (choice in choices) {
                Row(Modifier.padding(8.dp, 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected eqI choice, onClick = { selected = onUpdate(choice) })
                    Text(choice, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (!description.isNullOrBlank())
            Text(description, Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun MpvCheckbox(title: String, value: Boolean, description: String? = null, enabled: Boolean = true, onUpdate: (Boolean) -> Unit = {}) {
    Column(Modifier.padding(10.dp, 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = value, enabled = enabled, onCheckedChange = { onUpdate(it) })
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
        if (!description.isNullOrBlank())
            Text(description, Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelMedium)
    }
}