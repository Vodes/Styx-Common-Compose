package moe.styx.common.compose.utils

import kotlinx.serialization.Serializable
import moe.styx.common.compose.extensions.find
import moe.styx.common.data.Category
import moe.styx.common.data.Favourite
import moe.styx.common.data.Media
import moe.styx.common.extension.eqI
import moe.styx.common.extension.equalsAny

@Serializable
enum class SortType(val displayName: String) {
    ADDED("Added"),
    NAME("Name"),
    ENGLISH("English Name"),
    ROMAJI("Romaji Name")
}

@Serializable
data class SearchState(
    val search: String = "",
    val selectedGenres: List<String> = emptyList(),
    val selectedCategories: List<Category> = emptyList(),
    val sortType: SortType = SortType.ADDED,
    val sortDescending: Boolean = true
) {
    fun filterMedia(mediaList: List<Media>, favourites: List<Favourite> = emptyList()): List<Media> {
        var filtered = mediaList
        val lowerSearch = search.trim().lowercase()

        if (favourites.isEmpty() && selectedCategories.isNotEmpty()) {
            val selectedCategoryIds = selectedCategories.asSequence()
                .map { it.GUID.lowercase() }
                .toSet()
            filtered = filtered.filter { media ->
                (media.categoryID?.lowercase() ?: "") in selectedCategoryIds
            }
        }

        if (favourites.isEmpty() && selectedGenres.isNotEmpty()) {
            val selectedGenreNames = selectedGenres.asSequence()
                .map { it.lowercase() }
                .toSet()
            filtered = filtered.filter { media ->
                val genresOfMedia = media.genres
                    ?.split(",")
                    ?.asSequence()
                    ?.map { it.trim().lowercase() }
                    ?.toSet()
                    .orEmpty()
                genresOfMedia.any { it in selectedGenreNames }
            }
        }

        if (lowerSearch.length > 2)
            filtered = filtered.filter { it.find(lowerSearch) }

        return if (favourites.isNotEmpty() && sortType == SortType.ADDED) {
            val favouritesByMediaId = favourites.associateBy { it.mediaID.lowercase() }
            if (sortDescending)
                filtered.sortedByDescending { m ->
                    val fav = favouritesByMediaId[m.GUID.lowercase()]
                    fav?.added ?: 0L
                }
            else
                filtered.sortedBy { m ->
                    val fav = favouritesByMediaId[m.GUID.lowercase()]
                    fav?.added ?: 0L
                }
        } else {
            when (sortType) {
                SortType.ADDED -> if (sortDescending) filtered.sortedByDescending { it.added } else filtered.sortedBy { it.added }
                SortType.NAME -> if (sortDescending) filtered.sortedByDescending { it.name.lowercase() } else filtered.sortedBy { it.name.lowercase() }
                SortType.ROMAJI -> if (sortDescending) filtered.sortedByDescending {
                    it.nameJP?.lowercase() ?: ""
                } else filtered.sortedBy { it.nameJP?.lowercase() ?: "" }

                SortType.ENGLISH -> if (sortDescending) filtered.sortedByDescending {
                    it.nameEN?.lowercase() ?: ""
                } else filtered.sortedBy { it.nameEN?.lowercase() ?: "" }
            }
        }
    }
}
