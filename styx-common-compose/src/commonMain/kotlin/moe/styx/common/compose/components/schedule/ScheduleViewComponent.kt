package moe.styx.common.compose.components.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import moe.styx.common.compose.components.anime.AnimeListItem
import moe.styx.common.compose.extensions.dayOfWeek
import moe.styx.common.compose.extensions.getTargetTime
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Media
import moe.styx.common.data.ScheduleWeekday
import moe.styx.common.extension.capitalize
import moe.styx.common.extension.eqI
import moe.styx.common.extension.padString

@Composable
fun ScheduleViewComponent(
    modifier: Modifier = Modifier,
    dayLabelModifier: Modifier = Modifier.padding(4.dp, 3.dp),
    itemColModifier: Modifier = Modifier,
    onClick: (Media) -> Unit
) {
    val nav = LocalGlobalNavigator.current
    val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
    val storage by sm.storageFlow.collectAsState()

    val days = ScheduleWeekday.entries.toTypedArray()
    val mapped = remember {
        days.map { day ->
            day to storage.scheduleList.filter { it.getTargetTime().dayOfWeek == day.dayOfWeek() }.mapNotNull {
                val res = storage.mediaList.find { m -> m.GUID eqI it.mediaID }
                res?.let { m -> it to m }
            }
        }.filter { it.second.isNotEmpty() }
    }
    val images = remember {
        storage.imageList.filter { img -> mapped.find { it.second.find { it.second.thumbID eqI img.GUID } != null } != null }
    }

    AnimatedVisibility(mapped.isNotEmpty()) {
        LazyColumn(modifier.fillMaxSize()) {
            mapped.forEachIndexed { index, mappedDay ->
                item {
                    Column {
                        if (index != 0)
                            HorizontalDivider(
                                Modifier.fillMaxWidth().padding(8.dp),
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
                            )

                        Text(
                            mappedDay.first.name.capitalize(),
                            modifier = dayLabelModifier,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
                items(mappedDay.second, key = { it.second.GUID }) { mediaPair ->
                    val (schedule, media) = mediaPair
                    Column(itemColModifier.fillMaxWidth()) {
                        val target = schedule.getTargetTime()
                        Text(
                            "${target.hour.padString(2)}:${target.minute.padString(2)}",
                            modifier = Modifier.padding(6.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Column(Modifier.padding(6.dp, 1.dp)) {
                            AnimeListItem(
                                media,
                                images.find { it.GUID eqI media.thumbID },
                                schedule.finalEpisodeCount
                            ) { onClick(media) }
                        }
                    }
                }
                if (index == mapped.size - 1)
                    item { Spacer(Modifier.height(5.dp)) }
            }
        }
    }
}