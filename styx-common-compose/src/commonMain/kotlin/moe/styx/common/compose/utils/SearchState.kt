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

        if (favourites.isEmpty() && selectedCategories.isNotEmpty()) {
            filtered = filtered.filter { media ->
                selectedCategories.find { it.GUID eqI media.categoryID } != null
            }
        }

        if (favourites.isEmpty() && selectedGenres.isNotEmpty()) {
            filtered = filtered.filter { media ->
                val genresOfMedia = if (media.genres != null) media.genres!!.split(",") else listOf()
                selectedGenres.find { it.equalsAny(genresOfMedia) } != null
            }
        }

        if (search.isNotBlank() && search.length > 2)
            filtered = filtered.filter { it.find(search) }

        return if (favourites.isNotEmpty() && sortType == SortType.ADDED) {
            if (sortDescending)
                filtered.sortedByDescending { m ->
                    val fav = favourites.find { it.mediaID eqI m.GUID }
                    fav?.added ?: 0L
                }
            else
                filtered.sortedBy { m ->
                    val fav = favourites.find { it.mediaID eqI m.GUID }
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