package moe.styx.common.compose.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import moe.styx.common.compose.components.buttons.PopButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    title: String,
    addPopButton: Boolean = true,
    actions: @Composable () -> Unit = {},
    bottomBarContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = { Text(title, overflow = TextOverflow.Ellipsis, maxLines = 2) },
            actions = {
                actions()
                if (addPopButton)
                    PopButton()
            }
        )
    }, bottomBar = { bottomBarContent?.let { it() } }) {
        Box(Modifier.fillMaxSize().padding(it)) {
            content()
        }
    }
}