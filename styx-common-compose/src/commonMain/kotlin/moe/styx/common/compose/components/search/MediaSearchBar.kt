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
import kotlinx.coroutines.launch
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.compose.utils.SortType
import moe.styx.common.data.Category
import moe.styx.common.util.launchGlobal

class MediaSearch(
    private val searchStore: KStore<SearchState>,
    private val initialState: SearchState,
    private val availableGenres: List<String>,
    private val availableCategories: List<Category>,
    private val favs: Boolean = false
) {
    private val _stateEmitter = MutableStateFlow(initialState)
    val stateEmitter: MutableStateFlow<SearchState>
        get() = _stateEmitter

    val showFilters = mutableStateOf(false)

    @Composable
    fun Component(modifier: Modifier = Modifier, handleFiltersInBar: Boolean = true) {
        var showFilters by remember { showFilters }
        val scope = rememberCoroutineScope()
        var internalState by remember { mutableStateOf(initialState) }

        OutlinedTextField(
            modifier = modifier, singleLine = true, value = internalState.search, shape = AppShapes.medium, label = { Text("Search") },
            onValueChange = {
                scope.launch {
                    internalState = internalState.copy(search = it)
                    _stateEmitter.emit(internalState)
                    launchGlobal { searchStore.set(internalState) }
                }
            },
            leadingIcon = {
                Icon(Icons.Default.Search, "Search")
            },
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
                SortDropdown(showSort, internalState, onDismiss = { showSort = false }) {
                    scope.launch {
                        internalState = internalState.copy(sortType = it.first, sortDescending = it.second)
                        _stateEmitter.emit(internalState)
                        launchGlobal { searchStore.set(internalState) }
                    }
                }
            })

        if (handleFiltersInBar) {
            AnimatedVisibility(showFilters) {
                GenreCategoryFilters()
            }
        }
    }

    @Composable
    fun GenreCategoryFilters() {
        val scope = rememberCoroutineScope()
        var internalState by remember { mutableStateOf(initialState) }
        Surface(Modifier.fillMaxWidth().padding(7.dp)) {
            ElevatedCard(Modifier.fillMaxWidth().padding(3.dp)) {
                Column {
                    Text("Category", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                    CategoryFilterBar(internalState, availableCategories) {
                        scope.launch {
                            internalState = internalState.copy(selectedCategories = it)
                            _stateEmitter.emit(internalState)
                            launchGlobal { searchStore.set(internalState) }
                        }
                    }

                    Text("Genre", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                    GenreFilterBar(internalState, availableGenres) {
                        scope.launch {
                            internalState = internalState.copy(selectedGenres = it)
                            _stateEmitter.emit(internalState)
                            launchGlobal { searchStore.set(internalState) }
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