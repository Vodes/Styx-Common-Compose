package moe.styx.common.compose.extensions

import kotlinx.coroutines.Job
import moe.styx.common.compose.components.tracking.common.CommonTrackingFunctions
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.MediaEntry
import moe.styx.common.util.launchThreaded

fun Job.joinAndSyncProgress(entry: MediaEntry, viewModel: MainDataViewModel) = launchThreaded {
    this@joinAndSyncProgress.join()
    CommonTrackingFunctions.syncProgressForEntry(entry.GUID, viewModel, true)
}

fun Job.joinAndSyncProgress(entryID: String, viewModel: MainDataViewModel) = launchThreaded {
    this@joinAndSyncProgress.join()
    CommonTrackingFunctions.syncProgressForEntry(entryID, viewModel, true)
}