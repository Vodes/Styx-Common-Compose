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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.compose.utils.SortType
import moe.styx.common.data.Category
import moe.styx.common.util.launchThreaded

class MediaSearch(
    private val searchStore: KStore<SearchState>,
    private val initialState: SearchState,
    private val availableGenres: List<String>,
    private val availableCategories: List<Category>,
    private val favs: Boolean = false
) {
    val stateEmitter = MutableStateFlow(initialState)

    @Composable
    fun Component(modifier: Modifier = Modifier) {
        var showFilters by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        OutlinedTextField(modifier = modifier, singleLine = true, value = initialState.search, shape = AppShapes.medium, label = { Text("Search") },
            onValueChange = {
                scope.launch {
                    stateEmitter.collectLatest { state ->
                        val new = state.copy(search = it)
                        stateEmitter.emit(new)
                        launchThreaded { searchStore.set(new) }
                    }
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
                SortDropdown(showSort, initialState, onDismiss = { showSort = false }) {
                    scope.launch {
                        stateEmitter.collectLatest { state ->
                            val new = state.copy(sortType = it.first, sortDescending = it.second)
                            stateEmitter.emit(new)
                            launchThreaded { searchStore.set(new) }
                        }
                    }
                }
            })

        AnimatedVisibility(showFilters) {
            filterBars(scope)
        }
    }

    @Composable
    private fun filterBars(scope: CoroutineScope) {
        Surface(Modifier.fillMaxWidth().padding(7.dp)) {
            ElevatedCard(Modifier.fillMaxWidth().padding(3.dp)) {
                Column {
                    Text("Category", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                    CategoryFilterBar(initialState, availableCategories) {
                        scope.launch {
                            stateEmitter.collectLatest { state ->
                                val new = state.copy(selectedCategories = it)
                                stateEmitter.emit(new)
                                launchThreaded { searchStore.set(new) }
                            }
                        }
                    }

                    Text("Genre", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                    GenreFilterBar(initialState, availableGenres) {
                        scope.launch {
                            stateEmitter.collectLatest { state ->
                                val new = state.copy(selectedGenres = it)
                                stateEmitter.emit(new)
                                launchThreaded { searchStore.set(new) }
                            }
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