package moe.styx.common.compose.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import moe.styx.common.compose.extensions.isFav
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.data.Media

@Composable
fun FavouriteIconButton(media: Media, modifier: Modifier = Modifier) {
    var isFav by remember { mutableStateOf(media.isFav()) }
    IconButton({
        if (!isFav)
            RequestQueue.addFav(media)
        else
            RequestQueue.removeFav(media)
        isFav = !isFav
    }) {
        if (isFav)
            Icon(Icons.Filled.Star, "Fav")
        else
            Icon(Icons.Outlined.StarOutline, "Not fav")
    }
}