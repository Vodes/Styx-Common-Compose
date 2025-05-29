package moe.styx.common.compose.utils

import moe.styx.common.data.DeviceInfo

expect fun fetchDeviceInfo(): DeviceInfo

expect fun openURI(uri: String?)