package moe.styx.common.compose.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.compose.viewmodels.MainDataViewModelStorage
import moe.styx.common.data.Media
import moe.styx.common.extension.eqI
import moe.styx.common.util.launchThreaded

@Composable
fun FavouriteIconButton(
    media: Media,
    mainDataViewModel: MainDataViewModel,
    mainDataViewModelStorage: MainDataViewModelStorage,
    modifier: Modifier = Modifier
) {
    val isFav = remember(mainDataViewModelStorage) { mainDataViewModelStorage.favouritesList.find { it.mediaID eqI media.GUID } != null }
    IconButton({
        launchThreaded {
            val jobs = if (!isFav)
                RequestQueue.addFav(media)
            else
                RequestQueue.removeFav(media)
            jobs?.let {
                it.first.join()
                mainDataViewModel.updateData(true)
            }
        }
    }) {
        if (isFav)
            Icon(Icons.Filled.Star, "Fav")
        else
            Icon(Icons.Outlined.StarOutline, "Not fav")
    }
}