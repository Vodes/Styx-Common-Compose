package moe.styx.common.compose.components.about.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.Platform
import moe.styx.common.compose.components.about.LibCard
import moe.styx.common.compose.components.about.LibRow
import moe.styx.common.compose.components.tracking.common.ElevatedSurface

@Composable
internal fun LibrariesSection() {
    LibCard(
        "Styx-Common-Compose",
        "Collection of compose related stuff used in both Styx clients",
        "MPL-2.0",
        "https://github.com/Vodes/Styx-Common-Compose",
        "Vodes"
    )
    LibCard(
        "Compose Multiplatform",
        "The multiplatform implementation of Jetpack Compose, the main UI framework on modern android apps, by Jetbrains.",
        "Apache-2.0",
        "https://github.com/JetBrains/compose-multiplatform",
        "JetBrains, Google"
    )
    LibCard(
        "Ktor",
        "An asynchronous framework for creating microservices, web applications and more. Written in Kotlin from the ground up.",
        "Apache-2.0",
        "https://github.com/ktorio/ktor",
        "JetBrains"
    )
    LibCard(
        "okio",
        "A modern I/O library for Android, Java, and Kotlin Multiplatform.",
        "Apache-2.0",
        "https://github.com/square/okio",
        "Square"
    )
    if (Platform.current == Platform.ANDROID)
        LibCard(
            "libmpv-android",
            "A libmpv implementation for Android that embeds the mpv player.",
            "MIT",
            "https://github.com/jarnedemeulemeester/libmpv-android",
            "jarnedemeulemeester"
        )
    if (Platform.current == Platform.JVM) {
        ElevatedSurface(Modifier.padding(2.dp, 6.dp).fillMaxWidth()) {
            Column(Modifier.padding(8.dp)) {
                Text("Others", style = MaterialTheme.typography.headlineSmall)

                LibRow("KDiscord-IPC", "Kotlin library for interacting with Discord", "https://github.com/caoimhebyrne/KDiscordIPC")
                LibRow("Zip4j", "A Java library for zip files / streams", "https://github.com/srikanth-lingala/zip4j")
                LibRow(
                    "junixsocket",
                    "A Java/JNI library that allows the use of Unix Domain Sockets",
                    "https://github.com/kohlschutter/junixsocket"
                )
            }
        }
    }
    Text("And many others that would bloat this screen. Check the Git Repo of the App to find out!", Modifier.padding(4.dp))
}