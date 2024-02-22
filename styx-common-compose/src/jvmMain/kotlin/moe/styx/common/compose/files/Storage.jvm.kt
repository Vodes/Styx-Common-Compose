package moe.styx.common.compose.files

import okio.FileSystem

actual val SYSTEMFILES: FileSystem
    get() = FileSystem.SYSTEM