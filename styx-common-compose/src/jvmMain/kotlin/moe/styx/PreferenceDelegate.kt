package moe.styx

import java.util.prefs.Preferences

object PreferenceDelegate {
    val delegate = Preferences.userNodeForPackage(this.javaClass)
}