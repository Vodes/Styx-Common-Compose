package moe.styx.common.compose.components.tracking.common

import kotlinx.coroutines.CancellationException

internal class TrackingSheetRequests(
    private val setLoading: (Boolean) -> Unit,
    private val setError: (String) -> Unit
) {
    private var activeRequests = 0

    suspend fun run(action: suspend () -> Unit) {
        activeRequests++
        setLoading(true)
        try {
            action()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            setError(trackingErrorMessage(exception.message))
        } finally {
            activeRequests--
            setLoading(activeRequests > 0)
        }
    }
}

internal fun trackingErrorMessage(message: String?): String =
    message?.takeIf { it.isNotBlank() } ?: "Failed to communicate with the tracking service."
