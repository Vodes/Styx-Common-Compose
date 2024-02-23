package moe.styx.common.compose.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.extensions.isFav
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.updateList
import moe.styx.common.compose.http.login
import moe.styx.common.data.Favourite
import moe.styx.common.data.Media
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI

@Composable
fun FavouriteIconButton(media: Media, modifier: Modifier = Modifier) {
    var isFav by remember { mutableStateOf(media.isFav()) }
    IconButton({
        //TODO: Implement with new request queue
//        if (!isFav)
//            RequestQueue.addFav(media)
//        else
//            RequestQueue.removeFav(media)
//        isFav = media.isFav()
//        favsTab.searchState.value = favsTab.mediaSearch.getDefault(updateList = DataManager.media.value.filter { it.isFav() })
        runBlocking {
            Storage.stores.favouriteStore.updateList { list ->
                if (isFav)
                    list.removeAll { it.mediaID eqI media.GUID }
                else
                    list.add(Favourite(media.GUID, login?.userID ?: "", currentUnixSeconds()))
            }
            isFav = !isFav
        }
    }) {
        if (isFav)
            Icon(Icons.Filled.Star, "Fav")
        else
            Icon(Icons.Outlined.StarOutline, "Not fav")
    }
}