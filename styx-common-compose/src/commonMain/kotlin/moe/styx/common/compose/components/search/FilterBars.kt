package moe.styx.common.compose.components.search

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import moe.styx.common.compose.components.misc.PrimarySelectableObject
import moe.styx.common.compose.components.misc.SecondarySelectableObject
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.data.Category

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterBar(initialState: SearchState, availableCategories: List<Category>, onChange: (List<Category>) -> Unit) {
    val selected = initialState.selectedCategories.toMutableStateList()
    selected.forEach {
        if (it !in availableCategories)
            selected.remove(it)
    }
    FlowRow {
        for (category in availableCategories.sortedByDescending { it.sort }) {
            PrimarySelectableObject(category.name, mutableStateOf(category in selected)) {
                if (it)
                    selected.add(category)
                else
                    selected.remove(category)
                onChange(selected.toList())
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreFilterBar(initialState: SearchState, availableGenres: List<String>, onChange: (List<String>) -> Unit) {
    val selected = initialState.selectedGenres.toMutableStateList()
    selected.forEach {
        if (it !in availableGenres)
            selected.remove(it)
    }
    FlowRow {
        for (genre in availableGenres.sortedBy { it.lowercase() }) {
            SecondarySelectableObject(genre, mutableStateOf(genre in selected)) {
                if (it)
                    selected.add(genre)
                else
                    selected.remove(genre)
                onChange(selected.toList())
            }
        }
    }
}