package moe.styx.common.compose.components.tracking.common

import moe.styx.common.compose.components.tracking.common.CommonMediaListStatus.Companion.fromAnilistStatus
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
    }
}

fun MediaListStatus.toCommon(): CommonMediaListStatus = fromAnilistStatus(this)

data class CommonMediaStatus(
    val entryID: Int,
    val mediaID: Int,
    val status: CommonMediaListStatus,
    val progress: Int = -1,
    val knownMax: Int = Int.MAX_VALUE
) {
    val hasProgress get() = progress > 0
    val hasKnownMax get() = knownMax != Int.MAX_VALUE
}