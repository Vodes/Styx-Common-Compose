package moe.styx.common.compose.utils

import kotlinx.serialization.Serializable
import moe.styx.common.data.Category

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
)