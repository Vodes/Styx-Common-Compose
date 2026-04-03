package moe.styx.common.compose.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
    val currentState: SearchState
        get() = _stateEmitter.value
    val genres: List<String>
        get() = availableGenres
    val categories: List<Category>
        get() = availableCategories
    val supportsFilters: Boolean
        get() = !favs

    fun updateState(transform: (SearchState) -> SearchState) {
        val updatedState = transform(_stateEmitter.value)
        if (updatedState == _stateEmitter.value) {
            return
        }
        _stateEmitter.value = updatedState
        launchGlobal { searchStore.set(updatedState) }
    }

    @Composable
    fun Component(modifier: Modifier = Modifier) {
        var showFilters by remember { mutableStateOf(false) }
        val internalState by stateEmitter.collectAsState(initialState)

        OutlinedTextField(modifier = modifier, singleLine = true, value = internalState.search, shape = AppShapes.medium, label = { Text("Search") },
            onValueChange = {
                updateState { state -> state.copy(search = it) }
            },
            leadingIcon = {
                Icon(Icons.Default.Search, "Search")
            },
            trailingIcon = {
                var showSort by remember { mutableStateOf(false) }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (!favs) {
                        IconButtonWithTooltip(if (showFilters) Icons.Filled.FilterAltOff else Icons.Filled.FilterAlt, "Show filters") {
                            showFilters = !showFilters
                        }
                    }
                    IconButtonWithTooltip(Icons.Filled.MoreVert, "Sorting") { showSort = true }
                }
                SortDropdown(showSort, internalState, onDismiss = { showSort = false }) {
                    updateState { state -> state.copy(sortType = it.first, sortDescending = it.second) }
                }
            })

        AnimatedVisibility(showFilters) {
            Surface(Modifier.fillMaxWidth().padding(7.dp)) {
                ElevatedCard(Modifier.fillMaxWidth().padding(3.dp)) {
                    Column {
                        Text("Category", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                        CategoryFilterBar(internalState, availableCategories) {
                            updateState { state -> state.copy(selectedCategories = it) }
                        }

                        Text("Genre", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                        GenreFilterBar(internalState, availableGenres) {
                            updateState { state -> state.copy(selectedGenres = it) }
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
