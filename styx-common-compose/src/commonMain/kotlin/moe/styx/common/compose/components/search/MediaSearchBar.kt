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
import io.github.xxfast.kstore.KStore
import kotlinx.coroutines.flow.MutableStateFlow
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.compose.utils.SortType
import moe.styx.common.data.Category
import moe.styx.common.util.launchThreaded

@Composable
fun MediaSearchBar(
    searchStore: KStore<SearchState>,
    initialState: SearchState,
    receiverStateFlow: MutableStateFlow<SearchState>,
    modifier: Modifier = Modifier,
    availableCategories: List<Category>,
    availableGenres: List<String>,
    favs: Boolean = false,
) {
    var showFilters by remember { mutableStateOf(false) }
    var currentState by mutableStateOf(initialState)

    OutlinedTextField(currentState.search,
        modifier = modifier,
        singleLine = true,
        onValueChange = {
            launchThreaded {
                currentState = currentState.copy(search = it)
                receiverStateFlow.emit(currentState)
                searchStore.set(currentState)
            }
        }, shape = AppShapes.medium, label = {
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
            SortDropdown(showSort, currentState, onDismiss = { showSort = false }) {
                launchThreaded {
                    currentState = currentState.copy(sortType = it.first, sortDescending = it.second)
                    receiverStateFlow.emit(currentState)
                    searchStore.set(currentState)
                }
            }
        }
    )

    AnimatedVisibility(showFilters) {
        Surface(Modifier.fillMaxWidth().padding(7.dp)) {
            ElevatedCard(Modifier.fillMaxWidth().padding(3.dp)) {
                Column {
                    Text("Category", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                    CategoryFilterBar(currentState, availableCategories) {
                        launchThreaded {
                            currentState = currentState.copy(selectedCategories = it)
                            receiverStateFlow.emit(currentState)
                            searchStore.set(currentState)
                        }
                    }

                    Text("Genre", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                    GenreFilterBar(currentState, availableGenres) {
                        launchThreaded {
                            currentState = currentState.copy(selectedGenres = it)
                            receiverStateFlow.emit(currentState)
                            searchStore.set(currentState)
                        }
                    }
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