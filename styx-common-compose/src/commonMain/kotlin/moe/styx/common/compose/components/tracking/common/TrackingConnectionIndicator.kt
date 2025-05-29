package moe.styx.common.compose.components.tracking.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.utils.openURI

@Composable
fun TrackingConnectionIndicator(modifier: Modifier = Modifier, username: String?, siteName: String, userBaseURL: String) {
    FilterChip(
        !username.isNullOrBlank(),
        onClick = {
            if (username == null) return@FilterChip
            openURI("$userBaseURL/$username")
        },
        enabled = !username.isNullOrBlank(),
        label = {
            Text(siteName)
        },
        leadingIcon = {
            if (username == null)
                Icon(Icons.Default.LinkOff, "No user connected for $siteName", tint = MaterialTheme.colorScheme.error)
            else
                Icon(Icons.Default.Link, "$siteName user: $username", tint = MaterialTheme.colorScheme.primary)
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            labelColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp),
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier.padding(5.dp, 2.dp)
    )
}