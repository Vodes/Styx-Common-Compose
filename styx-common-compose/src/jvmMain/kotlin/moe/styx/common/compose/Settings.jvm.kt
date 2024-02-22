package moe.styx.common.compose

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import moe.styx.PreferenceDelegate

private var internalSettings: Settings? = null

actual val settings: Settings
    get() {
        if (internalSettings == null) {
            internalSettings = PreferencesSettings(PreferenceDelegate.delegate)
        }
        return internalSettings!!
    }