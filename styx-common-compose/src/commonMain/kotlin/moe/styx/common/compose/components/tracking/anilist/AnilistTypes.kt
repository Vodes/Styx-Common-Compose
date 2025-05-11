package moe.styx.common.compose.components.tracking.anilist

import pw.vodes.anilistkmp.MediaListEntry
import pw.vodes.anilistkmp.graphql.fragment.MediaBig
import pw.vodes.anilistkmp.graphql.fragment.User

typealias AlMedia = MediaBig
typealias AlUserEntry = MediaListEntry
typealias AlMediaList = List<MediaBig>
typealias AlUserMediaEntries = List<MediaListEntry>
typealias AlUser = User