package moe.styx.common.compose.components.tracking.common

import moe.styx.common.compose.components.tracking.common.CommonMediaListStatus.Companion.fromAnilistStatus
import moe.styx.common.util.Log
import pw.vodes.anilistkmp.graphql.type.MediaListStatus

enum class CommonMediaListStatus {
    WATCHING,
    PLANNING,
    COMPLETED,
    DROPPED,
    PAUSED,
    REPEATING,
    NONE;

    fun toAnilistStatus(): MediaListStatus? = when (this) {
        WATCHING -> MediaListStatus.CURRENT
        PLANNING -> MediaListStatus.PLANNING
        COMPLETED -> MediaListStatus.COMPLETED
        DROPPED -> MediaListStatus.DROPPED
        PAUSED -> MediaListStatus.PAUSED
        REPEATING -> MediaListStatus.REPEATING
        else -> null
    }

    fun toMalStatus(): Pair<String, Boolean>? = when (this) {
        WATCHING -> "watching" to false
        PLANNING -> "plan_to_watch" to false
        COMPLETED -> "completed" to false
        DROPPED -> "dropped" to false
        PAUSED -> "on_hold" to false
        REPEATING -> "watching" to true
        else -> null
    }

    companion object {
        fun fromAnilistStatus(status: MediaListStatus): CommonMediaListStatus = when (status) {
            MediaListStatus.CURRENT -> WATCHING
            MediaListStatus.PLANNING -> PLANNING
            MediaListStatus.COMPLETED -> COMPLETED
            MediaListStatus.DROPPED -> DROPPED
            MediaListStatus.PAUSED -> PAUSED
            MediaListStatus.REPEATING -> REPEATING
            else -> NONE
        }

        fun fromMalStatus(status: String, isRewatching: Boolean): CommonMediaListStatus = when (status.lowercase()) {
            "watching" -> if (!isRewatching) WATCHING else REPEATING
            "completed" -> COMPLETED
            "dropped" -> DROPPED
            "on_hold" -> PAUSED
            "plan_to_watch" -> PLANNING
            else -> NONE
        }
    }
}

fun MediaListStatus.toCommon(): CommonMediaListStatus = fromAnilistStatus(this)

data class CommonMediaStatus(
    val entryID: Int,
    val mediaID: Int,
    val status: CommonMediaListStatus,
    val progress: Int = -1,
    val knownMax: Int = Int.MAX_VALUE,
    val score: Float? = 0.0F
) {
    val hasProgress get() = progress > 0
    val hasKnownMax get() = knownMax != Int.MAX_VALUE
}

data class CommonTrackingResult(val success: Boolean, val message: String? = null, val exception: Exception? = null) {
    init {
        if (!message.isNullOrBlank() || exception != null) {
            if (!success) {
                Log.e(exception = exception) { message ?: "Failed to update tracking on remote site!" }
            } else {
                Log.d { message ?: "Updated tracking data on remote site!" }
            }
        }
    }
}