package moe.styx.common.compose.components.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.islandtime.toInstant
import moe.styx.common.compose.components.anime.AnimeListItem
import moe.styx.common.compose.extensions.dayOfWeek
import moe.styx.common.compose.extensions.getTargetTime
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.getCurrentAndCollectFlow
import moe.styx.common.data.Media
import moe.styx.common.data.ScheduleWeekday
import moe.styx.common.extension.capitalize
import moe.styx.common.extension.eqI
import moe.styx.common.extension.padString

@Composable
fun ScheduleDay(day: ScheduleWeekday, onClick: (Media) -> Unit) {
    val mediaList by Storage.stores.mediaStore.getCurrentAndCollectFlow()
    val schedules by Storage.stores.scheduleStore.getCurrentAndCollectFlow()
    val filtered = schedules.filter { it.getTargetTime().dayOfWeek == day.dayOfWeek() }.sortedBy { it.getTargetTime().toInstant().secondOfUnixEpoch }
    if (filtered.isEmpty())
        return
    Column(Modifier.padding(2.dp, 6.dp)) {
        Text(
            day.name.capitalize(),
            modifier = Modifier.padding(2.dp),
            style = MaterialTheme.typography.titleLarge
        )
        for (schedule in filtered) {
            val media = mediaList.find { it.GUID eqI schedule.mediaID } ?: continue
            val target = schedule.getTargetTime()
            Text(
                "${target.hour.padString(2)}:${target.minute.padString(2)}",
                modifier = Modifier.padding(6.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Column(Modifier.padding(6.dp, 1.dp)) { AnimeListItem(media, schedule.finalEpisodeCount) { onClick(media) } }
        }
    }
}