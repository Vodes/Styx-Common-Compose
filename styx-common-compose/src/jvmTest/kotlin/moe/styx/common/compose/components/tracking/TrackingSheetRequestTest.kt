package moe.styx.common.compose.components.tracking

import io.github.hristogochev.vortex.model.ScreenModelStore
import io.ktor.client.plugins.ConnectTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.components.tracking.anilist.AnilistBottomSheetModel
import moe.styx.common.compose.components.tracking.mal.MALBottomSheetModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrackingSheetRequestTest {
    private class Model(
        val name: String,
        val isLoading: () -> Boolean,
        val error: () -> String?,
        val request: suspend (suspend () -> Unit) -> Unit,
    )

    private suspend fun withModels(block: suspend (Model) -> Unit) {
        try {
            val mal = MALBottomSheetModel()
            val anilist = AnilistBottomSheetModel()
            val models = listOf(
                Model("MAL", { mal.isLoading }, { mal.errorString }, mal::runTrackingRequest),
                Model("AniList", { anilist.isLoading }, { anilist.errorString }, anilist::runTrackingRequest),
            )
            models.forEach { block(it) }
        } finally {
            ScreenModelStore.dispose("standalone")
        }
    }

    @Test
    fun connectionTimeoutProducesErrorAndStopsLoading() = runBlocking {
        withModels { model ->
            val exception = ConnectTimeoutException("https://example.test", 10_000)
            model.request {
                assertTrue(model.isLoading(), model.name)
                throw exception
            }
            assertEquals(exception.message, model.error(), model.name)
            assertFalse(model.isLoading(), model.name)
        }
    }

    @Test
    fun exceptionWithoutMessageProducesVisibleError() = runBlocking {
        withModels { model ->
            model.request { throw Exception() }
            assertTrue(assertNotNull(model.error(), model.name).isNotBlank(), model.name)
            assertFalse(model.isLoading(), model.name)
        }
    }

    @Test
    fun cancellingRequestPropagatesCancellationAndStopsLoading() = runBlocking {
        withModels { model ->
            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                model.request { awaitCancellation() }
            }
            assertTrue(model.isLoading(), model.name)
            job.cancelAndJoin()
            assertTrue(job.isCancelled, model.name)
            assertNull(model.error(), model.name)
            assertFalse(model.isLoading(), model.name)
        }
    }

    @Test
    fun thrownCancellationIsRethrownWithoutError() = runBlocking {
        withModels { model ->
            val cancellation = CancellationException("cancelled")
            var caught: CancellationException? = null
            try {
                model.request { throw cancellation }
            } catch (exception: CancellationException) {
                caught = exception
            }
            assertEquals(cancellation, caught, model.name)
            assertNull(model.error(), model.name)
            assertFalse(model.isLoading(), model.name)
        }
    }

    @Test
    fun successfulRequestStopsLoadingWithoutError() = runBlocking {
        withModels { model ->
            var completed = false
            model.request {
                assertTrue(model.isLoading(), model.name)
                completed = true
            }
            assertTrue(completed, model.name)
            assertNull(model.error(), model.name)
            assertFalse(model.isLoading(), model.name)
        }
    }

    @Test
    fun overlappingRequestsKeepLoadingUntilBothFinish() = runBlocking {
        withModels { model ->
            val finishFirst = CompletableDeferred<Unit>()
            val finishSecond = CompletableDeferred<Unit>()
            val first = launch(start = CoroutineStart.UNDISPATCHED) {
                model.request { finishFirst.await() }
            }
            val second = launch(start = CoroutineStart.UNDISPATCHED) {
                model.request { finishSecond.await() }
            }
            try {
                assertTrue(model.isLoading(), model.name)
                finishFirst.complete(Unit)
                first.join()
                assertTrue(model.isLoading(), model.name)
                finishSecond.complete(Unit)
                second.join()
                assertFalse(model.isLoading(), model.name)
                assertNull(model.error(), model.name)
            } finally {
                first.cancel()
                second.cancel()
            }
        }
    }
}
