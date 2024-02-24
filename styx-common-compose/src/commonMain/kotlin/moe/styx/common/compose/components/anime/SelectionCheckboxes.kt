package moe.styx.common.compose.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.common.compose.extensions.desktopPointerEvent
import moe.styx.common.compose.extensions.onRightClick
import moe.styx.common.data.MediaEntry

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SelectionCheckboxes(showSelection: MutableState<Boolean>, selected: SnapshotStateMap<String, Boolean>, episodes: List<MediaEntry>, index: Int) {
    var checked by mutableStateOf(selected[episodes[index].GUID] ?: false)
    var isIn by mutableStateOf(false)
    var timeIn by mutableStateOf(0L)
    val scope = rememberCoroutineScope()
    AnimatedVisibility(showSelection.value) {
        val mod = Modifier.desktopPointerEvent({
            isIn = true
            scope.launch {
                while (isIn && timeIn < 85) {
                    timeIn++
                    delay(20)
                }
            }
        }, {
            isIn = false
            scope.launch {
                while (!isIn && timeIn > 0) {
                    timeIn--
                    delay(2)
                }
            }
        })

        Column {
            Column {
                AnimatedVisibility(timeIn > 35 && index > 0) {
                    IconButton({
                        episodes.filter { episodes.indexOf(it) < index }.forEach {
                            selected[it.GUID] = !(selected[episodes[index].GUID] ?: false)
                        }
                    }, modifier = mod) {
                        Icon(Icons.Default.KeyboardArrowUp, "")
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
            Checkbox(checked, modifier = Modifier.onRightClick { timeIn = 85 }, onCheckedChange = {
                checked = !checked
                selected[episodes[index].GUID] = checked
            })
            Column {
                AnimatedVisibility(timeIn > 35 && index < (episodes.size - 1)) {
                    Spacer(Modifier.height(2.dp))
                    IconButton({
                        episodes.filter { episodes.indexOf(it) > index }.forEach {
                            selected[it.GUID] = !(selected[episodes[index].GUID] ?: false)
                        }
                    }, modifier = mod) {
                        Icon(Icons.Default.KeyboardArrowDown, "")
                    }
                }
            }
        }
    }
}
