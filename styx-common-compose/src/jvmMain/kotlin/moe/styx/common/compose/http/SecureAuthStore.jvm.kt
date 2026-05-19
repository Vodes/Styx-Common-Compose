package moe.styx.common.compose.http

import eu.anifantakis.lib.ksafe.KSafe

actual object SecureAuthStore {
    private val store by lazy { KSafe() }

    actual suspend fun getString(key: String, default: String): String {
        return store.get(key, default)
    }

    actual suspend fun putString(key: String, value: String) {
        store.put(key, value)
    }
}
