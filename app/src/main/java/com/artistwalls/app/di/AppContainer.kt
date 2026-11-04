package com.artistwalls.app.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.artistwalls.app.data.download.DownloadManager
import com.artistwalls.app.data.local.ArtistWallsDatabase
import com.artistwalls.app.data.local.LibraryDao
import com.artistwalls.app.data.local.PreferencesStore
import com.artistwalls.app.data.model.Wallpaper
import com.artistwalls.app.data.remote.SuggestionService
import com.artistwalls.app.data.remote.WikimediaApi
import com.artistwalls.app.data.repo.ArtistImageCache
import com.artistwalls.app.data.repo.WallpaperRepository
import com.artistwalls.app.data.share.ShareHelper
import com.artistwalls.app.data.wallpaper.WallpaperSetter
import com.artistwalls.app.data.model.FxSettings
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

/**
 * Hand-rolled dependency container: one instance per process, no annotation
 * processing, no reflection, nothing to keep in sync at build time.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cache(Cache(File(appContext.cacheDir, "http_cache"), 64L * 1024 * 1024))
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json,image/*")
                .build()
            chain.proceed(request)
        }
        .apply {
            if (com.artistwalls.app.BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                )
            }
        }
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val api: WikimediaApi = Retrofit.Builder()
        .baseUrl(COMMONS_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(WikimediaApi::class.java)

    val wallpaperRepository = WallpaperRepository(api)
    val artistImageCache = ArtistImageCache(wallpaperRepository)
    val suggestionService = SuggestionService(okHttpClient, json)

    private val database = ArtistWallsDatabase.get(appContext)
    val libraryDao: LibraryDao = database.libraryDao()

    val downloadManager = DownloadManager(appContext, okHttpClient, libraryDao)
    val wallpaperSetter = WallpaperSetter(appContext)
    val shareHelper = ShareHelper(appContext)
    val preferences = PreferencesStore(appContext, json)

    /** Hand-off slot so a tapped tile can open instantly, before any refetch. */
    val selection = SelectionHolder()

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Live visual-effect switches, shared by every screen. */
    val fx = FxController(appScope, preferences)

    companion object {
        const val COMMONS_BASE_URL = "https://commons.wikimedia.org/"
        const val USER_AGENT =
            "ArtistWalls/1.0 (Android; wallpaper app; contact: nihaonihao54059@gmail.com)"
    }
}

/** Holds the photograph the user just tapped, so the detail screen opens instantly. */
class SelectionHolder {
    private val _current = MutableStateFlow<Wallpaper?>(null)
    val current: StateFlow<Wallpaper?> = _current.asStateFlow()

    fun select(wallpaper: Wallpaper) {
        _current.value = wallpaper
    }

    fun find(id: String?): Wallpaper? = _current.value?.takeIf { it.id == id }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer has not been provided")
}

/** Current effect switches, so composables can style themselves without a ViewModel. */
val LocalFxSettings = staticCompositionLocalOf { FxSettings() }

/**
 * Owns the four switches: mirrors them into a StateFlow the UI observes, and writes
 * every change straight to DataStore so the choice survives a restart.
 */
class FxController(
    private val scope: CoroutineScope,
    private val preferences: PreferencesStore
) {
    private val _settings = MutableStateFlow(FxSettings())
    val settings: StateFlow<FxSettings> = _settings.asStateFlow()

    init {
        scope.launch {
            preferences.fxSettings.collect { stored -> _settings.value = stored }
        }
    }

    fun update(transform: (FxSettings) -> FxSettings) {
        val next = transform(_settings.value)
        _settings.value = next
        scope.launch { preferences.saveFx(next) }
    }
}
