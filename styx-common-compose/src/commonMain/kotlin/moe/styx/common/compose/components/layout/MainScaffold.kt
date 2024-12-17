package moe.styx.common.compose.components.layout

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.PopButton

@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    title: String,
    addPopButton: Boolean = true,
    actions: @Composable (RowScope.() -> Unit) = {},
    bottomBarContent: (@Composable () -> Unit)? = null,
    addAnimatedTitleBackground: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
        targetValue = MaterialTheme.colorScheme.surfaceColorAtElevation(11.dp),
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "title-color"
    )
    MainScaffold(modifier, {
        if (!addAnimatedTitleBackground)
            Text(title, overflow = TextOverflow.Ellipsis, maxLines = 2)
        else {
            Surface(Modifier.clip(AppShapes.medium), shadowElevation = 1.dp, color = animatedColor) {
                Text(title, Modifier.padding(5.dp), overflow = TextOverflow.Ellipsis, maxLines = 2)
            }
        }
    }, addPopButton, actions, bottomBarContent, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    titleContent: @Composable () -> Unit = {},
    addPopButton: Boolean = true,
    actions: @Composable (RowScope.() -> Unit) = {},
    bottomBarContent: (@Composable () -> Unit)? = null,
    topAppBarModifier: Modifier = Modifier,
    topAppBarExpandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    content: @Composable () -> Unit
) {
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = {
                titleContent()
            },
            actions = {
                actions()
                if (addPopButton)
                    PopButton()
            },
            expandedHeight = topAppBarExpandedHeight,
            modifier = topAppBarModifier
        )
    }, bottomBar = { bottomBarContent?.let { it() } }) {
        Box(Modifier.fillMaxSize().padding(it)) {
            content()
        }
    }
}