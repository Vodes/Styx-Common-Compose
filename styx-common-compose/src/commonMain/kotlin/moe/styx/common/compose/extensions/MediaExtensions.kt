package moe.styx.common.compose.extensions

import io.github.xxfast.kstore.extensions.getOrEmpty
import io.islandtime.*
import io.islandtime.clock.now
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.files.Storage
import moe.styx.common.data.Category
import moe.styx.common.data.Media
import moe.styx.common.data.MediaSchedule
import moe.styx.common.data.ScheduleWeekday
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toBoolean
import moe.styx.common.util.isClose

fun Media.isFav(): Boolean = runBlocking {
    val favs = Storage.stores.favouriteStore.getOrEmpty()
    return@runBlocking favs.find { it.mediaID eqI this@isFav.GUID } != null
}

fun ScheduleWeekday.dayOfWeek(): DayOfWeek {
    return when (this) {
        ScheduleWeekday.MONDAY -> DayOfWeek.MONDAY
        ScheduleWeekday.TUESDAY -> DayOfWeek.TUESDAY
        ScheduleWeekday.WEDNESDAY -> DayOfWeek.WEDNESDAY
        ScheduleWeekday.THURSDAY -> DayOfWeek.THURSDAY
        ScheduleWeekday.FRIDAY -> DayOfWeek.FRIDAY
        ScheduleWeekday.SATURDAY -> DayOfWeek.SATURDAY
        else -> DayOfWeek.SUNDAY
    }
}

fun MediaSchedule.getTargetTime(): ZonedDateTime {
    val now = DateTime.now().at(TimeZone("Europe/Berlin"))
    val adjusted = now.copy(hour = this.hour, minute = this.minute)
    val target = adjusted.next(this.day.dayOfWeek())
    return target.adjustedTo(TimeZone.systemDefault())
}

fun Media.find(search: String): Boolean {
    return name.isClose(search.trim()) || nameEN.isClose(search.trim()) || nameJP.isClose(search.trim())
}

fun List<Media>.getDistinctGenres(): List<String> {
    return this.flatMap { m ->
        (m.genres ?: "").split(",").map { it.trim() }
    }.distinct().filter { it.isNotBlank() }.sorted().toList()
}

fun List<Media>.getDistinctCategories(allCategories: List<Category>): List<Category> {
    return this.asSequence().map { (it.categoryID ?: "").trim() }
        .distinct().filter { it.isNotBlank() }
        .mapNotNull { allCategories.find { cat -> cat.GUID eqI it } }
        .filter { it.isVisible.toBoolean() }.sortedBy { it.sort }.toList()
}