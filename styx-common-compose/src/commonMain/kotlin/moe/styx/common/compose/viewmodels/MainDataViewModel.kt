package moe.styx.common.compose.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.get
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.components.tracking.anilist.AlUser
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.Stores
import moe.styx.common.compose.files.getBlocking
import moe.styx.common.compose.http.MalApiClient
import moe.styx.common.compose.http.checkLogin
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.*
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.util.Log
import moe.styx.libs.mal.ext.fetching.fetchCurrentUser
import moe.styx.libs.mal.types.MALUser
import pw.vodes.anilistkmp.AnilistApiClient
import pw.vodes.anilistkmp.ext.fetchViewer
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainDataViewModel : ScreenModel {
    private val _storageFlow = MutableStateFlow(MainDataViewModelStorage())
    val storageFlow =
        _storageFlow.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), MainDataViewModelStorage())

    val loadingStateFlow = Storage.loadingProgress.stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), "")
    private val _isLoadingStateFlow = MutableStateFlow(false)
    val isLoadingStateFlow = _isLoadingStateFlow.combine(Storage.isLoaded.asStateFlow()) { a, b -> a || !b }
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(2000), false)

    var anilistApiClient by mutableStateOf<AnilistApiClient?>(null)
        private set
    var anilistUser by mutableStateOf<AlUser?>(null)
        private set

    var malApiClient by mutableStateOf<MalApiClient?>(null)
        private set
    var malUser by mutableStateOf<MALUser?>(null)
        private set

    init {
        Log.d { "Initializing MainDataViewModel" }
        startUpdateLoop()
    }

    private var checkTimeout = 0

    private fun startUpdateLoop() = screenModelScope.launch {
        _isLoadingStateFlow.emit(true)
        updateData()
        while ((ServerStatus.continueChecking || login == null) && isActive) {
            delay(2000)
            checkTimeout++;
            if (ServerStatus.lastKnown in arrayOf(ServerStatus.UNKNOWN, ServerStatus.ERROR) && checkTimeout >= 4) {
                _isLoadingStateFlow.emit(false)
                this.cancel()
            }
        }
        ensureActive()
        updateData(forceUpdate = true, updateStores = true)
        _isLoadingStateFlow.emit(false)

        delay(20000)
        while (isActive) {
            delay(5.toDuration(DurationUnit.MINUTES))
            ensureActive()
            Log.d("MainDataViewModel") { "Running automatic data refresh." }
            _isLoadingStateFlow.emit(true)
            updateData(updateStores = true)
            _isLoadingStateFlow.emit(false)
        }
    }

    fun updateData(forceUpdate: Boolean = false, updateStores: Boolean = false) = screenModelScope.launch {
        if (updateStores) {
            launch { runAnilistCheck() }
            launch { runMALCheck() }
            Log.d { "Updating storage with stores..." }
            launch { Storage.loadData() }.also { Storage.refreshDataJob = it }.join()
            Storage.refreshDataJob = null
        } else
            Log.d { "Updating storage without stores..." }
        if (forceUpdate)
            _storageFlow.emit(getUpdatedStorage())
        else
            _storageFlow.getAndUpdate { getUpdatedStorage(it.updated) }
    }

    private fun getUpdatedStorage(unixSeconds: Long? = null): MainDataViewModelStorage {
        return MainDataViewModelStorage(
            Stores.mediaStore.getBlocking(),
            Stores.entryStore.getBlocking(),
            Stores.imageStore.getBlocking(),
            Stores.categoryStore.getBlocking(),
            Stores.favouriteStore.getBlocking(),
            Stores.watchedStore.getBlocking(),
            Stores.scheduleStore.getBlocking(),
            Stores.proxyServerStore.getBlocking(),
            Stores.mediaPreferencesStore.getBlocking(),
            unixSeconds ?: currentUnixSeconds()
        )
    }

    fun getMediaStorageForEntryID(entryID: String, storage: MainDataViewModelStorage): Pair<MediaEntry, MediaStorage> {
        val entry = storage.entryList.find { it.GUID eqI entryID }!!
        return entry to getMediaStorageForID(entry.mediaID, storage)
    }

    fun getMediaStorageForID(id: String, storage: MainDataViewModelStorage): MediaStorage {
        Log.d { "Fetching metadata for: $id" }
        val media = storage.mediaList.find { it.GUID eqI id }!!
        val prequel = if (media.prequel.isNullOrBlank()) null else storage.mediaList.find { it.GUID eqI media.prequel }
        val sequel = if (media.sequel.isNullOrBlank()) null else storage.mediaList.find { it.GUID eqI media.sequel }
        val filtered = storage.entryList.filter { it.mediaID eqI media.GUID }
        val prefs = storage.userMediaPreferences.find { it.mediaID eqI media.GUID }
        val sortAsc = prefs?.mediaPreferences?.sortEpisodesAscendingly ?: settings["episode-asc", false]
        val entries = if (sortAsc) filtered.sortedBy {
            it.entryNumber.toDoubleOrNull() ?: 0.0
        } else filtered.sortedByDescending { it.entryNumber.toDoubleOrNull() ?: 0.0 }
        return MediaStorage(
            media,
            storage.imageList.find { it.GUID eqI media.thumbID },
            prequel,
            prequel?.let { storage.imageList.find { it.GUID eqI prequel.thumbID } },
            sequel,
            sequel?.let { storage.imageList.find { it.GUID eqI sequel.thumbID } },
            entries,
            prefs?.mediaPreferences
        )
    }

    fun reauthorizeStyx() = screenModelScope.launch {
        val token = if (!appConfig().debugToken.isNullOrBlank()) {
            appConfig().debugToken!!
        } else {
            settings["refreshToken", ""]
        }
        val attempt = checkLogin(token)
        if (attempt != null) {
            login = attempt
            runAnilistCheck()
            runMALCheck()
        }
    }

    internal suspend fun runAnilistCheck() {
        if (login != null && ServerStatus.lastKnown != ServerStatus.UNKNOWN) {
            if (login?.anilistData == null) {
                anilistApiClient = AnilistApiClient().also { publicAnilistApiClient = it }
                anilistUser = null
                publicAnilistUser = null
                return
            } else if (anilistUser == null) {
                Log.d { "Logging in to AniList..." }
                anilistApiClient = AnilistApiClient(login!!.anilistData!!.accessToken).also { publicAnilistApiClient = it }
                val viewerResp = anilistApiClient!!.fetchViewer()
                if (viewerResp.data == null) {
                    Log.e(exception = viewerResp.exception) { "Could not login to AniList user!" }
                } else {
                    anilistUser = viewerResp.data.also { publicAnilistUser = it }
                    Log.d { "Logged in to AniList as: ${anilistUser!!.name} (${anilistUser!!.id})" }
                }
            }
        }
    }

    internal suspend fun runMALCheck() {
        if (login != null && ServerStatus.lastKnown != ServerStatus.UNKNOWN) {
            if (login?.malData == null) {
                malApiClient = null
                publicMalApiClient = null
                malUser = null
                publicMalUser = null
                return
            } else if (malUser == null) {
                Log.d { "Logging in to MyAnimeList..." }
                malApiClient = MalApiClient(login!!.malData!!).also { publicMalApiClient = it }
                val userResp = malApiClient!!.fetchCurrentUser()
                if (!userResp.isSuccess || userResp.data == null) {
                    return
                }
                malUser = userResp.data!!.also { publicMalUser = it }
                Log.d { "Logged in to MyAnimeList as: ${malUser!!.name} (${malUser!!.id})" }
            }
        }
    }

    companion object {
        var publicAnilistApiClient: AnilistApiClient? = null
        var publicAnilistUser: AlUser? = null

        var publicMalApiClient: MalApiClient? = null
        var publicMalUser: MALUser? = null
    }
}

data class MediaStorage(
    val media: Media,
    val image: Image?,
    val prequel: Media?,
    val prequelImage: Image?,
    val sequel: Media?,
    val sequelImage: Image?,
    val entries: List<MediaEntry>,
    val preferences: MediaPreferences?,
) {
    fun hasPrequel(): Boolean = prequel != null && prequelImage != null
    fun hasSequel(): Boolean = sequel != null && sequelImage != null
    fun prequelPair(): Pair<Media, Image?> = prequel!! to prequelImage
    fun sequelPair(): Pair<Media, Image?> = sequel!! to sequelImage
}

data class MainDataViewModelStorage(
    val mediaList: List<Media> = emptyList(),
    val entryList: List<MediaEntry> = emptyList(),
    val imageList: List<Image> = emptyList(),
    val categoryList: List<Category> = emptyList(),
    val favouritesList: List<Favourite> = emptyList(),
    val watchedList: List<MediaWatched> = emptyList(),
    val scheduleList: List<MediaSchedule> = emptyList(),
    val proxyServerList: List<ProxyServer> = emptyList(),
    val userMediaPreferences: List<UserMediaPreferences> = emptyList(),
    val updated: Long = 0L
) {
    override fun hashCode() =
        (updated + mediaList.size + entryList.size + imageList.size + categoryList.size + favouritesList.size + watchedList.size + scheduleList.size + proxyServerList.size + userMediaPreferences.size).hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is MainDataViewModelStorage)
            return super.equals(other)
        return updated == other.updated &&
                mediaList.size == other.mediaList.size &&
                entryList.size == other.entryList.size &&
                imageList.size == other.imageList.size &&
                watchedList.size == other.watchedList.size &&
                favouritesList.size == other.favouritesList.size &&
                categoryList.size == other.categoryList.size &&
                scheduleList.size == other.scheduleList.size &&
                proxyServerList.size == other.proxyServerList.size &&
                userMediaPreferences.size == other.userMediaPreferences.size
    }
}