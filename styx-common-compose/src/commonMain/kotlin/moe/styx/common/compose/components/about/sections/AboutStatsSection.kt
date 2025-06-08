package moe.styx.common.compose.components.about.sections

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.about.BasicChip
import moe.styx.common.compose.viewmodels.MainDataViewModelStorage
import moe.styx.common.extension.toBoolean

@Composable
internal fun StatsSection(storage: MainDataViewModelStorage) {
    val chipColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.6f)
    val uniqueShows = remember { storage.mediaList.filter { it.isSeries.toBoolean() }.filter { it.prequel.isNullOrBlank() }.size }
    val movies = remember { storage.mediaList.filter { !it.isSeries.toBoolean() }.size }
    val episodes = remember { storage.entryList.size - movies }
    Text("Stats", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(5.dp))
    FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
        BasicChip("Shows: $uniqueShows", Icons.Default.LiveTv, "Shows", chipColor)
        BasicChip("Episodes: $episodes", Icons.Default.VideoLibrary, "Episodes", chipColor)
        BasicChip("Movies: $movies", Icons.Default.Movie, "Movies", chipColor)
    }
}