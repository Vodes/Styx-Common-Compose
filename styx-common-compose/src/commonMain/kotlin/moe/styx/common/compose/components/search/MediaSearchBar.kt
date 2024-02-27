package moe.styx.common.compose.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.compose.utils.SortType
import moe.styx.common.data.Category

@Composable
fun MediaSearchBar(
    modifier: Modifier = Modifier,
    availableCategories: List<Category>,
    favs: Boolean = false,
    movies: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val searchStateStore = if (!favs && !movies) Storage.stores.showSearchState else
        (if (favs) Storage.stores.favSearchState else Storage.stores.movieSearchState)
    val currentSearchState by searchStateStore.updates.collectAsState(SearchState())
    var showFilters by remember { mutableStateOf(false) }

    OutlinedTextField(currentSearchState?.search ?: "",
        modifier = modifier,
        onValueChange = {
            scope.launch { searchStateStore.set((currentSearchState ?: SearchState()).copy(search = it)) }
        }, label = {
            Text("Search")
        }, leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            var showSort by remember { mutableStateOf(false) }
            Row {
                if (!favs) {
                    IconButtonWithTooltip(if (showFilters) Icons.Filled.FilterAltOff else Icons.Filled.FilterAlt, "Show filters") {
                        showFilters = !showFilters
                    }
                }
                IconButtonWithTooltip(Icons.Filled.MoreVert, "Sorting") { showSort = true }
            }
            SortDropdown(showSort, currentSearchState ?: SearchState(), onDismiss = { showSort = false }) {
                scope.launch { searchStateStore.set((currentSearchState ?: SearchState()).copy(sortType = it.first, sortDescending = it.second)) }
            }
        }
    )

    AnimatedVisibility(showFilters) {
        Surface(Modifier.fillMaxWidth().padding(7.dp)) {
            ElevatedCard(Modifier.fillMaxWidth().padding(3.dp)) {
                Column {
                    Text("Category", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
//                    CategoryFilterBar(list, shows) {
//                        selectedCategories = it
//                        result(updateList(sort, search, descending, selectedCategories, selectedGenres))
//                    }

                    Text("Genre", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
//                    GenreFilterBar(list, shows) {
//                        selectedGenres = it
//                        result(updateList(sort, search, descending, selectedCategories, selectedGenres))
//                    }
                }
            }
        }
    }
}

@Composable
fun SortDropdown(expanded: Boolean, initialState: SearchState, onDismiss: () -> Unit, onChange: (Pair<SortType, Boolean>) -> Unit) {
    val initialType = initialState.sortType
    val descending = initialState.sortDescending
    DropdownMenu(expanded, onDismissRequest = onDismiss) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sort by", Modifier.padding(15.dp, 10.dp))
            IconButtonWithTooltip(
                if (descending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                if (descending) "Sort ascending" else "Sort descending"
            ) { onChange(initialType to !descending) }
        }
        SortType.entries.forEach { type ->
            DropdownMenuItem(
                text = { Text(type.displayName) },
                enabled = initialType != type,
                onClick = {
                    onChange(type to descending)
                })
        }
    }
}